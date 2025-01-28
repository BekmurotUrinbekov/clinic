package uz.clinic

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.Date
import java.time.Duration

fun getCurrentUser(): User {
    val authentication = SecurityContextHolder.getContext().authentication
    SecurityContextHolder.getContext().authentication.credentials
    if (authentication != null && authentication.principal is User) {
        return (authentication.principal as User)
    }
    throw UserNotFoundException()
}


@Component
class JwtTokenUtils(
    private val customUserDetails: CustomUserDetailsService
) {

    @Value("\${security.jwt.secret-key}")
    private lateinit var secret: String

    @Value("\${jwt.lifetime}")
    private lateinit var jwtLifeTime: Duration

    @Value("\${ref.lifetime}")
    private lateinit var refLifeTime: Duration

    fun generateToken(userDetails: UserDetails): String {
        val claims = mutableMapOf<String, Any>()
//        val rolesList = userDetails.authorities.map { "ROLE_" + it.authority }
        val rolesList = userDetails.authorities.map { it.authority }
        claims["roles"] = rolesList
        val issuedDate = Date()
        val expiredDate = Date(issuedDate.time + jwtLifeTime.toMillis())

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.username)
            .setIssuedAt(issuedDate)
            .setExpiration(expiredDate)
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact()
    }

    fun generateTokenWithRefreshToken(refreshToken: String): String {
        val claims = getAllClaimsFromToken(refreshToken)
        val username = claims["username"].toString()
        val userDetails = customUserDetails.loadUserByUsername(username)
        return generateToken(userDetails)
    }

    fun getUsername(token: String): String {
        return getAllClaimsFromToken(token).subject
    }

    fun getRoles(token: String): List<String> {
        return getAllClaimsFromToken(token)["roles"] as List<String>
    }

    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secret)
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun refreshToken(accessToken: String): String {
        val username = getUsername(accessToken)
        val claims = mapOf("username" to username)
        val issuedDate = Date()
        val expiredDate = Date(issuedDate.time + refLifeTime.toMillis())

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(issuedDate)
            .setExpiration(expiredDate)
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact()
    }
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        try {
            val claims = getAllClaimsFromToken(token)
            val username = claims.subject
            val expiration = claims.expiration

            if (username != userDetails.username) return false

            return !expiration.before(Date())
        } catch (e: Exception) {
            return false
        }
    }

}
