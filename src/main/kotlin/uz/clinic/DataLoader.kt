package uz.clinic

import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DataLoader(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
): CommandLineRunner {

    override fun run(vararg args: String?) {
        val password= passwordEncoder.encode("1234")
        val user = User("enzo",password,"fernandez", true,"","", LocalDate.now(),UserRole.DEV)
        userRepository.findByUserName(user.username)?: userRepository.save(user)
    }
}