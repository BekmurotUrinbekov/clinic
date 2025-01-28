package uz.clinic

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdAt: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var updatedAt: Date? = null,
    @CreatedBy var createdBy: Long? = null,
    @LastModifiedBy var updatedBy: Long? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false
)

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false,unique = true) var userName: String,
    var _password: String,
    var fullName: String,
    var gender: Boolean,
    var address: String,
    @Column(nullable = false,unique = true) var phoneNumber: String,
    var birthDate: LocalDate,
    @Enumerated(EnumType.STRING) var role: UserRole = UserRole.PATIENT,
    @ManyToOne val clinic: Clinic?=null
): BaseEntity(), UserDetails{
    override fun getAuthorities(): List<SimpleGrantedAuthority> = mutableListOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    override fun getPassword(): String = _password
    override fun getUsername(): String = userName
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}

@Entity
class Employee(
    var experience: Double,
    var education: String,
    @OneToOne val user: User,
    var consultantPrice: Double? = null,
    @ManyToOne var services: Services? = null,
    @ManyToMany val patients: MutableList<User> = mutableListOf(),
):BaseEntity()

@Entity
class Schedule(
    @ManyToOne val doctor: Employee,
    var day: LocalDate,
    var startTime: LocalTime,
    var endTime: LocalTime,
    var breakStart: LocalTime,
    var breakEnd: LocalTime,
    ): BaseEntity()

@Entity
class DiagnosisAnalysis(
    @ManyToOne val patient: User,
    @ManyToOne val doctor: Employee,
    @Enumerated(EnumType.STRING) val type: ResultName,
    var result: String,
): BaseEntity()

@Entity
class Appointment(
    @ManyToOne val patient: User,
    @ManyToOne val doctor: Employee,
    var startDate: LocalDate,
    var startTime: LocalTime,
    @Enumerated(EnumType.STRING) var status: AppointmentStatus = AppointmentStatus.PENDING,
):BaseEntity()

@Entity
class Transaction(
    @ManyToOne val patient: User,
    val amount: Double,
    @Enumerated(EnumType.STRING) val paymentMethod: PaymentMethod,
    @ManyToOne val doctor: Employee? = null,
    @ManyToOne var services: Services? = null
):BaseEntity()

@Entity
class Services(
    var name: String,
    var description: String,
    var price: Double,
    @ManyToOne val department: Department
):BaseEntity()

@Entity
class Department(
    @Column(nullable = false,unique = true) var name: String,
    @ManyToOne val clinic: Clinic

):BaseEntity()
@Entity
class Clinic(
    @Column(nullable = false,unique = true) var name: String,
    var address: String,
    var phoneNumber: String,
    var email: String,
):BaseEntity()
