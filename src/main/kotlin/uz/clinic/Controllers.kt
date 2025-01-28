package uz.clinic

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@ControllerAdvice
class ExceptionHandler(private val errorMessageSource: ResourceBundleMessageSource) {

    @ExceptionHandler(GenericException::class)
    fun handlingException(exception: GenericException): ResponseEntity<BaseMessage> {
        return ResponseEntity.badRequest().body(exception.getErrorMessage(errorMessageSource))
    }
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: AccessDeniedException,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<BaseMessage> {
        return ResponseEntity.badRequest().body(BaseMessage(HttpStatus.FORBIDDEN.value(), ex.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, List<String>>> {
        val errors = ex.bindingResult.fieldErrors
            .map { it.defaultMessage ?: "Invalid value" }

        return ResponseEntity(mapOf("errors" to errors), HttpHeaders(), HttpStatus.BAD_REQUEST)
    }
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(ex: HttpMessageNotReadableException): ResponseEntity<Map<String, String>> {
        val errorMessage = ex.cause?.message ?: ex.message ?: "Invalid JSON input"
        return ResponseEntity(
            mapOf("error" to errorMessage),
            HttpHeaders(),
            HttpStatus.BAD_REQUEST
        )
    }
}

@RestController
@RequestMapping("api/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun createUser(
        @RequestBody @Valid dto: UserDto
    ) = userService.create( dto)

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody @Valid updatedDto: UserUpdateDto
    ) = userService.update(id, updatedDto)

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) = userService.delete(id)

    @GetMapping
    fun getAllUsers(pageable: Pageable) = userService.getAll(pageable)

    @GetMapping("/{id}")
    fun findUserById(@PathVariable id: Long) = userService.findById(id)
}
@RestController
@RequestMapping("api/services")
class ServicesController(private val services: Servicess) {

    @PostMapping
    fun createUser(
        @RequestBody @Valid dto: ServiceDto
    ) = services.create( dto)

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody @Valid updatedDto: ServiceUpdateDto
    ) = services.update(id, updatedDto)

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) = services.delete(id)

    @GetMapping
    fun getAllUsers(pageable: Pageable) = services.findAll(pageable)

    @GetMapping("/{id}")
    fun findUserById(@PathVariable id: Long) = services.findById(id)
}
@RestController
@RequestMapping("api/diagnosis-analysis")
class DiagnosisAnalysisController(private val diagnosisService: DiagnosisAnalysisService) {
    @PostMapping
    fun create(
        @RequestBody @Valid dto: DiagnosisAnalysisDto
    ) = diagnosisService.create(dto)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid updatedDto: DiagnosisAnalysisUpdateDto
    ) = diagnosisService.update(id, updatedDto)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = diagnosisService.delete(id)

    @GetMapping
    fun getAll(pageable: Pageable) = diagnosisService.findAll(pageable)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long) = diagnosisService.findById(id)

    @GetMapping("patient/{id}")
    fun findByIdPatient(@PathVariable("id") patientId: Long) = diagnosisService.findByPatient(patientId)
}

@RestController
@RequestMapping("api/employees")
class EmployeeController(private val employeeService: EmployeeService) {

    @PostMapping
    fun createEmployee(
        @RequestBody @Valid dto: EmployeeDto
    ) = employeeService.create(dto)

    @PutMapping("/{id}")
    fun updateEmployee(
        @PathVariable id: Long,
        @RequestBody @Valid updatedDto: EmployeeUpdateDto
    ) = employeeService.update(id, updatedDto)

    @DeleteMapping("/{id}")
    fun deleteEmployee(@PathVariable id: Long) = employeeService.delete(id)

    @GetMapping
    fun getAllEmployees(pageable: Pageable) = employeeService.findAll(pageable)

    @GetMapping("/{id}")
    fun findEmployeeById(@PathVariable id: Long) = employeeService.findById(id)
}
@RestController
@RequestMapping("api/appointments")
class AppointmentController(private val appointmentService: AppointmentService) {

    @PostMapping
    fun createAppointment(
        @RequestParam doctorId: Long,
        @RequestBody @Valid dto: AppointmentDto
    ) = appointmentService.create(doctorId, dto)

    @PutMapping("/{id}")
    fun updateAppointment(
        @PathVariable id: Long,
        @RequestBody @Valid dto: AppointmentUpdateDto
    ) = appointmentService.update(id, dto)

    @DeleteMapping("/{id}")
    fun deleteAppointment(@PathVariable id: Long) = appointmentService.delete(id)

    @GetMapping
    fun getAllAppointments(
        @RequestParam status: AppointmentStatus,
        pageable: Pageable
    ) = appointmentService.findAll(status, pageable)

    @GetMapping("/doctor")
    fun getAllAppointmentsForDoctor(
        @RequestParam status: AppointmentStatus,
        pageable: Pageable
    ) = appointmentService.findAllForDoctor(status, pageable)

    @GetMapping("/{id}")
    fun findAppointmentById(@PathVariable id: Long) = appointmentService.findById(id)
}

@RestController
@RequestMapping("api/departments")
class DepartmentController(private val service: DepartmentService) {

    @PostMapping
    fun create(
        @RequestBody @Valid dto: DepartmentDto
    ) = service.create(dto)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid updatedDto: DepartmentUpdateDto
    ) = service.update(id, updatedDto)

    @DeleteMapping("/{id}")
    fun deleteEmployee(@PathVariable id: Long) = service.delete(id)

    @GetMapping
    fun getAllEmployees(pageable: Pageable) = service.findAll(pageable)

    @GetMapping("/{id}")
    fun findEmployeeById(@PathVariable id: Long) = service.findById(id)
}
@RestController
@RequestMapping("api/schedules")
class ScheduleController(private val scheduleService: ScheduleService) {

    @PostMapping
    fun createSchedule(
        @RequestBody @Valid dto: ScheduleDto
    ) = scheduleService.create(dto)

    @PutMapping("/{id}")
    fun updateSchedule(
        @PathVariable id: Long,
        @RequestBody @Valid dto: ScheduleUpdateDto
    ) = scheduleService.update(id, dto)

    @DeleteMapping("/{id}")
    fun deleteSchedule(@PathVariable id: Long) = scheduleService.delete(id)

    @GetMapping
    fun getAllSchedules(pageable: Pageable) = scheduleService.findAll(pageable)

    @GetMapping("/show/{doctorId}")
    fun getFreeTime(@PathVariable doctorId: Long, pageable: Pageable) = scheduleService.showFreeTimes(doctorId)

    @GetMapping("/{id}")
    fun findScheduleById(@PathVariable id: Long) = scheduleService.findById(id)
}
@RestController
@RequestMapping("api/clinics")
class ClinicController(private val clinicService: ClinicService) {

    @PostMapping
    fun createClinic(@RequestBody @Valid dto: ClinicDto) = clinicService.create(dto)

    @PutMapping("/{id}")
    fun updateClinic(
        @PathVariable id: Long,
        @RequestBody @Valid dto: ClinicUpdateDto
    ) = clinicService.update(id, dto)

    @DeleteMapping("/{id}")
    fun deleteClinic(@PathVariable id: Long) = clinicService.delete(id)

    @GetMapping
    fun getAllClinics(pageable: Pageable) = clinicService.findAll(pageable)

    @GetMapping("/{id}")
    fun findClinicById(@PathVariable id: Long) = clinicService.findById(id)
}
@RestController
@RequestMapping("api/transactions")
class TransactionController(private val transactionService: TransactionService) {
    @PostMapping("/{appointmentId}")
    fun createClinic(@PathVariable appointmentId:Long, @RequestBody @Valid dto: TransactionDto) = transactionService.payment(appointmentId, dto)

    @PostMapping
    fun create(@RequestBody @Valid dto: TransactionToServiceDto) = transactionService.paymentToService(dto)

    @GetMapping()
    fun getAll(@RequestParam("pay-for") showPayFor: ShowPayFor?,pageable: Pageable)= transactionService.findAll(showPayFor, pageable)

    @GetMapping("{id}")
    fun findById(@PathVariable id: Long) = transactionService.findById(id)

}
@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/token")
    fun generate(@RequestBody @Valid dto: AuthDto) = authService.generateToken(dto)

    @PostMapping
    fun generateWithRefreshToken(@RequestBody @Valid dto: RefreshTokenDto) = authService.generateTokenWithRefreshToken(dto)
}

