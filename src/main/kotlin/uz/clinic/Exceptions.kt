package uz.clinic

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource

sealed class GenericException() : RuntimeException() {

    abstract fun errorCode(): ErrorCode
    open fun getArguments(): Array<Any?>? = null

    fun getErrorMessage(resourceBundleMessageSource: ResourceBundleMessageSource): BaseMessage {
        val message = try {
            resourceBundleMessageSource.getMessage(
                errorCode().name, getArguments(), LocaleContextHolder.getLocale()
            )
        } catch (e: Exception) {
            e.message ?: "error"
        }
        return BaseMessage(errorCode().code, message)
    }
}

class UsernameInvalidException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.USERNAME_INVALID
}
class UserNotFoundException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.USER_NOT_FOUND
}
class UserAlreadyExistsException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.USERNAME_ALREADY_EXISTS
}
class EmployeeAlreadyExistsException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.EMPLOYEE_ALREADY_EXISTS
}
class EmployeeNotFoundException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.EMPLOYEE_NOT_FOUND
}
class ClinicNotFoundException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.CLINIC_NOT_FOUND
}
class ClinicAlreadyExistsException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.CLINIC_ALREADY_EXISTS
}
class DepartmentAlreadyExistsException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.DEPARTMENT_ALREADY_EXISTS
}
class DepartmentNotFoundException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.DEPARTMENT_NOT_FOUND
}
class ServiceNotFoundException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.SERVICE_NOT_FOUND
}
class ServiceAlreadyExistsException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.SERVICE_ALREADY_EXISTS
}
class ScheduleAlreadyExistsException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.SCHEDULE_ALREADY_EXISTS
}
class ScheduleNotFoundException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.SCHEDULE_NOT_FOUND
}
class ScheduleDontCorrectException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.SCHEDULE_DONT_CORRECT
}
class ScheduleAlreadyAppointmentsException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.SCHEDULE_ALREADY_APPOINTMENTS
}
class AppointmentNotFoundException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.APPOINTMENT_NOT_FOUND
}
class AppointmentAlreadyExistsException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.APPOINTMENT_ALREADY_EXISTS
}
class AppointmentDontFitToScheduleException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.APPOINTMENT_DONT_FIT_TO_SCHEDULE
}
class TransactionNotFoundException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.TRANSACTION_NOT_FOUND
}
class TransactionAlreadyExistsException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.TRANSACTION_ALREADY_EXISTS
}
class EmployeeCreatDontEnoughFieldException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.EMPLOYEE_CREATE_DONT_ENOUGH_FIELD
}
class DiagnosisAnalysisAlreadyExistsException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.DIAGNOSIS_ANALYSIS_ALREADY_EXISTS
}
class DiagnosisAnalysisNotFoundException : GenericException(){
    override fun errorCode(): ErrorCode = ErrorCode.DIAGNOSIS_ANALYSIS_NOT_FOUND
}