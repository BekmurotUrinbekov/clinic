package uz.clinic

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.util.Locale

@Configuration
class WebMvcConfig : WebMvcConfigurer {
    @Bean
    fun localeResolver() = SessionLocaleResolver().apply { setDefaultLocale(Locale("uz")) }

    @Bean
    fun errorMessageSource() = ResourceBundleMessageSource().apply {
        setDefaultEncoding(Charsets.UTF_8.name())
        setBasename("errors")
    }
}
@EnableWebSecurity
@Configuration
class SecurityApp(
    private val jwtRequestFilter: JwtRequestFilter,
    private val customUserDetails: CustomUserDetailsService
) {

    @Bean
    @Order(1)
    fun filterChainWithHTTPBasic(http: HttpSecurity): SecurityFilterChain {
        http.securityMatcher(AntPathRequestMatcher("/api/auth/**"))
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(HttpMethod.POST, "/api/auth/**" ).permitAll()
                    .requestMatchers(HttpMethod.PUT, "/user/**")
                    .authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.ALWAYS) }
            .csrf { it.ignoringRequestMatchers("/api/**") }
            .httpBasic { http }

        return http.build()
    }

    @Bean
    @Order(2)
    fun filterChainWithJWT(http: HttpSecurity): SecurityFilterChain {
        http.securityMatcher(AntPathRequestMatcher("/**"))
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(HttpMethod.POST,"api/user").permitAll()
                    .requestMatchers( "api/user/**").hasRole("DIRECTOR")
                    .requestMatchers("/api/employees/**").hasRole("DIRECTOR")
                    .requestMatchers("/api/services/**").hasRole("DIRECTOR")
                    .requestMatchers("/api/departments/**").hasRole("DIRECTOR")
                    .requestMatchers( "/api/clinics/**").hasRole("DEV")
                    .requestMatchers( "/api/schedules/**").hasRole("DOCTOR")
                    .requestMatchers( "/api/schedules/show/**").hasRole("PATIENT")
                    .requestMatchers( HttpMethod.POST,"/api/appointments/**").hasRole("PATIENT")
                    .requestMatchers( "/api/appointments/**").hasRole("DOCTOR")
                    .requestMatchers( "/api/transactions/**").hasRole("CASHIER")
                    .requestMatchers( "/api/diagnosis-analysis/**").hasAnyRole("LABORATORY", "DOCTOR")
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.ignoringRequestMatchers("/**", "/image/**") }
            .authenticationProvider(daoAuthenticationProvider())
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun daoAuthenticationProvider() = DaoAuthenticationProvider().apply {
        setPasswordEncoder(bCryptPasswordEncoder())
        setUserDetailsService(customUserDetails)
    }
}
@Component
class JwtRequestFilter(
    private val jwtTokenUtils: JwtTokenUtils,
    private val customUserDetails: CustomUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val jwt = authHeader.substring(7)
            val username = jwtTokenUtils.getUsername(jwt)

            if (username != null && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = customUserDetails.loadUserByUsername(username)

                if (jwtTokenUtils.validateToken(jwt, userDetails)) {
                    val authorities = userDetails.authorities
                    val authentication = UsernamePasswordAuthenticationToken(userDetails, null, authorities)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }
        filterChain.doFilter(request, response)
    }

}

