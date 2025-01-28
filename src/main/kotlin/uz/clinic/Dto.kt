package uz.clinic

import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalTime

data class BaseMessage(val code: Int, val message: String?)

data class UserDto(
    @field: NotBlank @field: NotNull val username: String,
    @field: NotBlank @field: NotNull var password: String,
    @field: NotBlank @field: NotNull val fullName: String,
    @field: NotNull val gender: Boolean,
    @field: NotBlank @field: NotNull val address: String,
    @field: Pattern(regexp = "^\\+998\\d{9}$") @field: NotBlank @field: NotNull val phoneNumber: String,
    @field: Past @field: NotNull val birthDate: LocalDate,
)

data class UserInfo(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val gender: Boolean,
    val address: String,
    val phoneNumber: String,
    val birthDate: LocalDate,
)
data class UserUpdateDto(
    @field: Size(min = 1) val username: String?,
    @field: Size(min = 1) val password: String?,
    @field: Size(min = 1) val fullName: String?,
    val gender: Boolean?,
    @field: Size(min = 1)  val address: String?,
    @field: Pattern(regexp = "^\\+998\\d{9}$") val phoneNumber: String?,
    @field:Past val birthDate: LocalDate?,
)
data class ClinicDto(
    @field: NotBlank @field: NotNull val name: String,
    @field: NotBlank @field: NotNull val address: String,
    @field: Pattern(regexp = "^\\+998\\d{9}$") @field: NotNull val phoneNumber: String,
    @field: Email @field: NotNull val email: String,
)
data class ClinicInfo(
    val id: Long,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val email: String,
)
data class ClinicUpdateDto(
    @field: Size(min = 1) val name: String?,
    @field: Size(min = 1) val address: String?,
    @field: Pattern(regexp = "^\\+998\\d{9}$") val phoneNumber: String?,
    @field: Email val email: String?,
)
data class EmployeeDto(
    @field: Valid @field: NotNull val user: UserDto,
    @field: NotNull val role: UserRole,
    @field: NotNull @field: Positive val experience: Double,
    @field: NotBlank @field:NotNull val education: String,
    @field: Positive var consultantPrice: Double?,
    @field: Positive var serviceId: Long?
)
data class EmployeeInfo(
    val id: Long,
    val user: UserInfo,
    val experience: Double,
    val education: String,
)
data class EmployeeUpdateDto(
    @field: Valid val user: UserUpdateDto?,
    val role: UserRole?,
    @field: Positive val experience: Double?,
    @field: Size(min = 1)  val education: String?,
)
data class AuthDto(
    @field:NotBlank @field:NotNull val username: String,
    @field:NotBlank @field:NotNull val password: String,
)
data class RefreshTokenDto(
    @field:NotBlank @field:NotNull val refreshToken: String
)
data class DepartmentDto(
    @field:NotBlank @field:NotNull val name: String,
)
data class DepartmentInfo(
    val id: Long,
    val name: String,
    val clinic: ClinicInfo
)
data class DepartmentUpdateDto(
    @field: Size(min = 1) val name: String,
)
data class DiagnosisAnalysisDto(
    @field: Positive @field:NotNull val transactionId: Long,
    @field:NotBlank @field:NotNull val result: String
)
data class DiagnosisAnalysisInfo(
    val id: Long,
    val patient: String,
    val doctor: String,
    val type: ResultName,
    val result: String,
)
data class DiagnosisAnalysisUpdateDto(
    @field:Size(min=1) val result: String?
)
data class ServiceDto(
    @field:NotBlank @field:NotNull val name: String,
    @field:NotBlank @field:NotNull val description: String,
    @field:Positive @field:NotNull val price: Double,
    @field:Positive @field:NotNull val departmentId: Long
)
data class ServiceUpdateDto(
    @field:Size(min = 1) val name: String,
    @field:Size(min = 1) val description: String,
    @field:Positive val price: Double,
)
data class ServiceInfo(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val  department: DepartmentInfo,
)
data class ScheduleDto(
    @field:Future @field:NotNull val day: LocalDate,
    @field: NotNull val startTime: LocalTime,
    @field: NotNull val endTime: LocalTime,
    @field: NotNull val breakStart: LocalTime
)
data class ScheduleInfo(
    val id: Long,
    val doctor: EmployeeInfo,
    val day: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val breakStart: LocalTime,
    val breakEnd: LocalTime,
)
data class AppointmentDto(
    @field:Future @field:NotNull  val startDate: LocalDate,
    @field:NotNull val startTime: LocalTime,
)
data class AppointmentInfo(
    val id: Long,
    val patient: String,
    val doctor: EmployeeInfo,
    val startDate: LocalDate,
    val startTime: LocalTime,
    val status: AppointmentStatus,
)
data class AppointmentUpdateDto(
    @field: Future val startDate: LocalDate?,
    val startTime: LocalTime?,
)
data class ScheduleUpdateDto(
    @field:Future val day: LocalDate?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val breakStart: LocalTime?
)
data class FreeTimeInfo(
    val from: LocalTime,
    val till: LocalTime,
)
data class TransactionDto(
    val paymentMethod: PaymentMethod
)
data class TransactionToServiceDto(
    @field:NotNull val patientId: Long,
    @field:NotNull val serviceId:Long,
    val paymentMethod: PaymentMethod,
    )
data class TransactionInfo(
    val id: Long,
    val patient: String,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val doctor: String?,
    val services: String?
)
data class TokenDto(
    val token: String,
    val refreshToken: String,
)