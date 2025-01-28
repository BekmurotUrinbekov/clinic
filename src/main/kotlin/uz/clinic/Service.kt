package uz.clinic

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime


interface UserService{
    fun create(dto: UserDto,role: UserRole? = null,clinic: Clinic? = null): UserInfo
    fun update(id: Long,updatedDto: UserUpdateDto,role: UserRole? = null): UserInfo
    fun delete(id: Long)
    fun getAll(pageable: Pageable): Page<UserInfo>
//    fun findByUsername(username: String): UserInfo
    fun findById(id: Long): UserInfo
}
interface EmployeeService{
    fun create(dto: EmployeeDto): EmployeeInfo
    fun update(id: Long,updatedDto: EmployeeUpdateDto): EmployeeInfo
    fun delete(id: Long)
    fun findById(id: Long): EmployeeInfo
    fun findAll(pageable: Pageable): Page<EmployeeInfo>
}
interface ClinicService{
    fun create(dto: ClinicDto): ClinicInfo
    fun update(id: Long,dto: ClinicUpdateDto): ClinicInfo
    fun delete(id: Long)
    fun findAll(pageable: Pageable): Page<ClinicInfo>
    fun findById(id: Long): ClinicInfo
}
interface ScheduleService{
    fun create(dto: ScheduleDto): ScheduleInfo
    fun update(id:Long,dto: ScheduleUpdateDto): ScheduleInfo
    fun delete(id: Long)
    fun findAll(pageable: Pageable): Page<ScheduleInfo>
    fun findById(id: Long): ScheduleInfo
    fun showFreeTimes(doctorId: Long):Map<LocalDate,List<FreeTimeInfo>>
}
interface AppointmentService{
    fun create(doctorId: Long,dto: AppointmentDto): AppointmentInfo
    fun update(id: Long,dto: AppointmentUpdateDto): AppointmentInfo
    fun delete(id: Long)
    fun findAll(status: AppointmentStatus,pageable: Pageable): Page<AppointmentInfo>
    fun findAllForDoctor(status: AppointmentStatus,pageable: Pageable): Page<AppointmentInfo>
    fun findById(id: Long): AppointmentInfo
}
interface DepartmentService{
    fun create(dto:DepartmentDto): DepartmentInfo
    fun update(id: Long,dto: DepartmentUpdateDto): DepartmentInfo
    fun delete(id: Long)
    fun findAll(pageable: Pageable): Page<DepartmentInfo>
    fun findById(id: Long): DepartmentInfo
}
interface DiagnosisAnalysisService{
    fun create(dto: DiagnosisAnalysisDto): DiagnosisAnalysisInfo
    fun update(id: Long,updateDto: DiagnosisAnalysisUpdateDto): DiagnosisAnalysisInfo
    fun delete(id: Long)
    fun findAll(pageable: Pageable): Page<DiagnosisAnalysisInfo>
    fun findById(id: Long): DiagnosisAnalysisInfo
    fun findByPatient(patientId: Long):List<DiagnosisAnalysisInfo>
}
interface Servicess{
    fun create(dto: ServiceDto): ServiceInfo
    fun update(id: Long,dto: ServiceUpdateDto): ServiceInfo
    fun delete(id: Long)
    fun findAll(pageable: Pageable): Page<ServiceInfo>
    fun findById(id: Long): ServiceInfo
}
interface TransactionService{
    fun payment(appointmentId: Long,transactionDto: TransactionDto): TransactionInfo
    fun paymentToService(dto: TransactionToServiceDto): TransactionInfo
    fun findAll(showPayFor: ShowPayFor?,pageable: Pageable):Page<TransactionInfo>
    fun findById(id:Long): TransactionInfo
}
interface AuthService{
    fun generateToken(dto: AuthDto): TokenDto
    fun generateTokenWithRefreshToken(dto: RefreshTokenDto): TokenDto
}

@Service
class UserServiceImpl(private val userRepository: UserRepository,
    private val passwordEncoder:BCryptPasswordEncoder): UserService{

    @Transactional
    override fun create(dto: UserDto,role: UserRole?,clinic: Clinic?): UserInfo {
        if (userRepository.existsByUserNameOrPhoneNumber(dto.username, dto.phoneNumber))
            throw UserAlreadyExistsException()
        dto.password = passwordEncoder.encode(dto.password)
        val user = userRepository.save(UserMapper.toEntity(dto,role,clinic))
        return UserMapper.toInfo(user)
    }
    @Transactional
    override fun update(id: Long,updatedDto: UserUpdateDto,role: UserRole?): UserInfo {
        val user = userRepository.findByIdAndDeletedFalseForDirector(id)?: throw UserNotFoundException()
        updatedDto.run {
            username?.let {
                userRepository.findByUserName(it)?.let { throw UserAlreadyExistsException() }
                user.userName = it
            }
            password?.let {user._password=passwordEncoder.encode(it)}
            fullName?.let {user.fullName=it}
            role?.let { user.role=it }
            gender?.let {user.gender=it}
            address?.let {user.address=it}
            phoneNumber?.let{
                userRepository.findByPhoneNumber(it)?.let { throw UserAlreadyExistsException() }
                user.phoneNumber=it}
            birthDate?.let { user.birthDate=it}
        }
        return UserMapper.toInfo(userRepository.save(user))
    }
    @Transactional
    override fun delete(id: Long) {
        userRepository.trashForDirector(id)
    }

    override fun getAll(pageable: Pageable): Page<UserInfo> {
        return userRepository.findAllNotDeletedForDirector(pageable).map { UserMapper.toInfo(it) }
    }

    override fun findById(id: Long): UserInfo {
        return userRepository.findByIdAndDeletedFalseForDirector(id)?.let { UserMapper.toInfo(it) }?: throw UserNotFoundException()
    }

}
@Service
class EmployeeServiceImpl(private val userService: UserService,
                          private val userRepository: UserRepository,
    private val employeeRepository: EmployeeRepository,
    private val serviceRepository: ServiceRepository): EmployeeService {

    @Transactional
    override fun create(dto: EmployeeDto): EmployeeInfo {
        dto.run {
            if ((role == UserRole.DOCTOR && (consultantPrice == null || serviceId == null)) || (role != UserRole.DOCTOR && (consultantPrice != null || serviceId != null)))
                throw EmployeeCreatDontEnoughFieldException()
            val user = userService.create(user, role, getCurrentUser().clinic)
                .let { userRepository.findByIdAndDeletedFalseForDirector(it.id) ?: throw UserNotFoundException() }
            val employee = EmployeeMapper.toEntity(this, user)
            serviceId?.let{
                employee.consultantPrice = consultantPrice
                employee.services = serviceRepository.findByIdAndDepartment_ClinicAndDeletedFalse(serviceId!!, getCurrentUser().clinic!!)
                    ?:throw ServiceNotFoundException()
            }

            return EmployeeMapper.toInfo(employeeRepository.save(employee))
        }
    }


    override fun update(id: Long,updatedDto: EmployeeUpdateDto): EmployeeInfo {
        val employee = employeeRepository.findByIdAndClinic(id,getCurrentUser().clinic!!) ?: throw EmployeeNotFoundException()
        updatedDto.run {
            userService.update(employee.user.id!!,user!!,role)
            experience?.let {employee.experience=it}
            education?.let { employee.education=it }
        }
        return EmployeeMapper.toInfo(employeeRepository.save(employee))
    }

    override fun delete(id: Long) {
        employeeRepository.findByIdAndClinic(id,getCurrentUser().clinic!!)?.let {
            userRepository.trashForDirector(it.user.id!!)
            it.deleted = true
            employeeRepository.save(it)
        }?: throw EmployeeNotFoundException()
    }

    override fun findById(id: Long): EmployeeInfo {
        return employeeRepository.findByIdAndClinic(id,getCurrentUser().clinic!!)?.let { EmployeeMapper.toInfo(it) }
            ?: throw EmployeeNotFoundException()
    }

    override fun findAll(pageable: Pageable): Page<EmployeeInfo> {
        return employeeRepository.findAllByClinic(getCurrentUser().clinic!!,pageable).map { EmployeeMapper.toInfo(it) }
    }
}
@Service
class AppointmentServiceImpl(private val appointmentRepository: AppointmentRepository,
    private val employeeRepository: EmployeeRepository,
    private val scheduleRepository: ScheduleRepository): AppointmentService{

    override fun create(doctorId: Long, dto: AppointmentDto): AppointmentInfo {
        val doctor = employeeRepository.findByIdAndDeletedFalse(doctorId)
            ?: throw EmployeeNotFoundException()
        dto.run {
            val schedule = scheduleRepository.findByDoctorAndDayAndDeletedFalse(doctor, startDate) ?: throw ScheduleNotFoundException()
            if (appointmentRepository.existsByDoctorAndStartDateAndTimeRange(doctor, startDate,startTime.minusMinutes(30),startTime.plusMinutes(30),getCurrentUser()))
                throw AppointmentAlreadyExistsException()
            if (!validateSchedule(startTime, startTime.plusMinutes(30), schedule))
                throw AppointmentDontFitToScheduleException()
        }
        val appointment = appointmentRepository.save(AppointmentMapper.toEntity(dto, doctor))
        return AppointmentMapper.toInfo(appointment)
    }

    override fun update(id: Long, dto: AppointmentUpdateDto): AppointmentInfo {
        val appointment = appointmentRepository.findByIdAndPatientAndStatusAndDeletedFalse(id, getCurrentUser(),AppointmentStatus.PENDING) ?: throw AppointmentNotFoundException()
        if (appointment.patient.id != getCurrentUser().id) throw AppointmentNotFoundException()
        dto.run {
            startTime?.let { appointment.startTime = it
            }
            startDate?.let { appointment.startDate = it}
        }
        appointment.run {
            val schedule = scheduleRepository.findByDoctorAndDayAndDeletedFalse(doctor, startDate)
                ?: throw ScheduleNotFoundException()
            if (appointmentRepository.existsByDoctorAndStartDateAndTimeRange(doctor, startDate, startTime.minusMinutes(30), startTime.plusMinutes(30),getCurrentUser()))
                throw AppointmentAlreadyExistsException()
            if (!validateSchedule(startTime, startTime.plusMinutes(30), schedule))
                throw AppointmentDontFitToScheduleException()
        }
        return AppointmentMapper.toInfo(appointmentRepository.save(appointment))
    }

    override fun delete(id: Long) {
        val appointment = appointmentRepository.findByIdAndPatientAndStatusAndDeletedFalse(id, getCurrentUser(),AppointmentStatus.PENDING) ?: throw AppointmentNotFoundException()
        appointment.deleted=true
        appointmentRepository.save(appointment)
    }

    override fun findAll(status: AppointmentStatus,pageable: Pageable): Page<AppointmentInfo> {
        return appointmentRepository.findAllByPatientAndStatusAndDeletedFalse(getCurrentUser(),status,pageable)
            .map { AppointmentMapper.toInfo(it) }
    }

    override fun findAllForDoctor(status: AppointmentStatus, pageable: Pageable): Page<AppointmentInfo> {
        return appointmentRepository.findAllByDoctor_UserAndStatusAndDeletedFalse(getCurrentUser(),status,pageable).map { AppointmentMapper.toInfo(it) }
    }

    override fun findById(id: Long): AppointmentInfo {
        appointmentRepository.findByIdAndPatientAndDeletedFalse(id,getCurrentUser())
            ?.let { return AppointmentMapper.toInfo(it) }?: throw AppointmentNotFoundException()
    }
    private fun validateSchedule(timeStart: LocalTime, timeEnd: LocalTime, schedule: Schedule): Boolean {
        val isWithinWorkingHours = !timeStart.isBefore(schedule.startTime) && !timeEnd.isAfter(schedule.endTime)
        val doesNotOverlapWithBreak = timeEnd.isBefore(schedule.breakStart) || timeStart.isAfter(schedule.breakEnd)
        return isWithinWorkingHours && doesNotOverlapWithBreak
    }
}
@Service
class DiagnosisAnalysisServiceImpl(private val repository: DiagnosisAnalysisRepository,
    private val transactionRepository: TransactionRepository,
    private val employeeRepository: EmployeeRepository): DiagnosisAnalysisService {

    override fun create(dto: DiagnosisAnalysisDto): DiagnosisAnalysisInfo {
        val transaction = transactionRepository.findByIdAndDeletedFalse(dto.transactionId)
            ?: throw TransactionNotFoundException()
        transaction.run {
            if (repository.existsByPatientAndDoctorAndCreatedAt_DateAndDeletedFalse(patient, getCurrentUser(), createdAt!!))
                throw DiagnosisAnalysisAlreadyExistsException()
            val diagnosisAnalysis: DiagnosisAnalysis
            if (getCurrentUser().role == UserRole.DOCTOR) {
                transaction.doctor?.let {
                    if (transaction.doctor.user.id != getCurrentUser().id) throw TransactionNotFoundException()
                    diagnosisAnalysis = DiagnosisAnalysis(patient,doctor!!, ResultName.DIAGNOSIS,dto.result)
                    return DiagnosisAnalysisMapper.toInfo(repository.save(diagnosisAnalysis))
                }?:  throw TransactionNotFoundException()
            }else{
                if (services?.department?.clinic?.id != getCurrentUser().clinic?.id) throw TransactionNotFoundException()
                val employee =
                    employeeRepository.findByUserAndDeletedFalse(getCurrentUser()) ?: throw EmployeeNotFoundException()
                diagnosisAnalysis = DiagnosisAnalysis(patient,employee, ResultName.ANALYSIS,dto.result)
                return DiagnosisAnalysisMapper.toInfo(repository.save(diagnosisAnalysis))
            }
        }
    }

    override fun update(id: Long, updateDto: DiagnosisAnalysisUpdateDto): DiagnosisAnalysisInfo {
        val diagnosisAnalysis = repository.findByIdAndDeletedFalse(id) ?: throw DiagnosisAnalysisNotFoundException()
        if (diagnosisAnalysis.doctor.user.id != getCurrentUser().id) throw DiagnosisAnalysisNotFoundException()
        updateDto.result?.let{diagnosisAnalysis.result = it}
        return DiagnosisAnalysisMapper.toInfo(repository.save(diagnosisAnalysis))
    }

    override fun delete(id: Long) {
        val diagnosisAnalysis = repository.findByIdAndDeletedFalse(id) ?: throw DiagnosisAnalysisNotFoundException()
        if (diagnosisAnalysis.doctor.user != getCurrentUser()) throw DiagnosisAnalysisNotFoundException()
        diagnosisAnalysis.deleted=true
        repository.save(diagnosisAnalysis)
    }

    override fun findAll(pageable: Pageable): Page<DiagnosisAnalysisInfo> {
        return repository.findAllNotDeletedForPageableForDoctor(pageable).map { DiagnosisAnalysisMapper.toInfo(it) }
    }

    override fun findById(id: Long): DiagnosisAnalysisInfo {
        return repository.findByIdAndDeletedFalseForDoctor(id)?.let{ DiagnosisAnalysisMapper.toInfo(it) } ?: throw DiagnosisAnalysisNotFoundException()
    }

    override fun findByPatient(patientId: Long): List<DiagnosisAnalysisInfo> {
        return repository.findAllByPatientIdAndDoctor_User_ClinicAndDeletedFalse(patientId,getCurrentUser().clinic!!).map { DiagnosisAnalysisMapper.toInfo(it) }
    }

}
@Service
class TransactionServiceImp(private val repository: TransactionRepository,
    private val appointmentRepository: AppointmentRepository,
    private val userRepository: UserRepository,
    private val serviceRepository: ServiceRepository): TransactionService{

    override fun payment(appointmentId: Long, dto: TransactionDto): TransactionInfo {
        val appointment =
            appointmentRepository.findByIdAndDeletedFalse(appointmentId) ?: throw AppointmentNotFoundException()
        appointment.run {
            val transaction: Transaction
            if (status == AppointmentStatus.PENDING) {
                appointment.status = AppointmentStatus.COMPLETED
                transaction = Transaction(patient, doctor.consultantPrice!!, dto.paymentMethod, doctor)
            }else throw TransactionAlreadyExistsException()
            return TransactionMapper.toInfo(repository.save(transaction))
        }
    }

    override fun paymentToService(dto: TransactionToServiceDto): TransactionInfo {
        dto.run {
            val patient = userRepository.findByIdAndDeletedFalse(patientId)?:throw UserNotFoundException()
            val services =
                serviceRepository.findByIdAndDepartment_ClinicAndDeletedFalse(serviceId, getCurrentUser().clinic!!)
                    ?: throw ServiceNotFoundException()
            val transaction = Transaction(patient, services.price, paymentMethod, services = services)
            return TransactionMapper.toInfo(repository.save(transaction))
        }
    }

    override fun findAll(showPayFor: ShowPayFor?, pageable: Pageable): Page<TransactionInfo> {
        showPayFor?.let{
            if (showPayFor == ShowPayFor.SERVICES) return repository.findAllByDoctorIsNullAndServices_Department_Clinic(getCurrentUser().clinic!!,pageable)
                .map { TransactionMapper.toInfo(it) }
            if (showPayFor == ShowPayFor.DOCTOR) return repository.findAllByServicesIsNullAndDoctor_User_Clinic(getCurrentUser().clinic!!,pageable)
                .map{ TransactionMapper.toInfo(it) }
        }
        return repository.findAllNotDeletedForPageableForDoctor(pageable).map {
            TransactionMapper.toInfo(it)
        }
    }

    override fun findById(id: Long): TransactionInfo {
        val transaction = repository.findByIdAndDeletedFalse(id) ?: throw TransactionNotFoundException()
        transaction.doctor?.run{
            if (user.clinic?.id != getCurrentUser().clinic?.id) throw TransactionNotFoundException()
        }
        transaction.services?.run {if (department?.clinic?.id != getCurrentUser().clinic?.id) throw TransactionNotFoundException()}
        return TransactionMapper.toInfo(transaction)
    }
}

@Service
class ClinicServiceImp(private val clinicRepository: ClinicRepository): ClinicService{
    override fun create(dto: ClinicDto): ClinicInfo {
        dto.run {
            if (clinicRepository.existsByNameOrAddressOrPhoneNumberOrEmail(name, address, phoneNumber, email))
                throw ClinicAlreadyExistsException()
        }
        val clinic = clinicRepository.save(ClinicMapper.toEntity(dto))
        return ClinicMapper.toInfo(clinic)
    }

    override fun update(id: Long,dto: ClinicUpdateDto): ClinicInfo {
        val clinic = clinicRepository.findById(id).orElseThrow { throw ClinicNotFoundException() }
        dto.run {
            if (clinicRepository.existsByNameOrAddressOrPhoneNumberOrEmail(name, address, phoneNumber, email))
                throw ClinicAlreadyExistsException()
            name?.let{clinic.name=it}
            address?.let { clinic.address=it }
            phoneNumber?.let { clinic.phoneNumber=it }
            email?.let { clinic.email=it }
        }
        return ClinicMapper.toInfo(clinicRepository.save(clinic))
    }

    override fun delete(id: Long) {
        val clinic = clinicRepository.findById(id).orElseThrow { throw ClinicNotFoundException() }
        clinic.deleted = true
        clinicRepository.save(clinic)
    }

    override fun findAll(pageable: Pageable): Page<ClinicInfo> {
        return clinicRepository.findAllPageableAndDeletedFalse(pageable).map { ClinicMapper.toInfo(it) }
    }

    override fun findById(id: Long): ClinicInfo {
        return clinicRepository.findByIdAndDeletedFalse(id)?.let { ClinicMapper.toInfo(it) }?: throw ClinicNotFoundException()
    }
}
@Service
class ScheduleServiceImp(private val repository: ScheduleRepository,
    private val employeeRepository: EmployeeRepository,
    private val appointmentRepository: AppointmentRepository): ScheduleService{

    override fun create(dto: ScheduleDto): ScheduleInfo {
        if (!validateSchedule(dto)) throw ScheduleDontCorrectException()
        val doctor =
            employeeRepository.findByUserAndDeletedFalse(getCurrentUser()) ?: throw EmployeeNotFoundException()
        repository.findByDoctorAndDayAndDeletedFalse(doctor, dto.day)?.let{ throw ScheduleAlreadyExistsException()}
        val schedule = repository.save(ScheduleMapper.toEntity(dto, doctor))
        return ScheduleMapper.toInfo(schedule)
    }

    override fun update(id:Long,dto: ScheduleUpdateDto): ScheduleInfo {
        val schedule = repository.findByIdAndDoctor_UserAndDeletedFalse(id, getCurrentUser())?:throw ScheduleNotFoundException()
        dto.run {
            day?.let { schedule.day = it}
            startTime?.let{schedule.startTime = it}
            endTime?.let { schedule.endTime = it}
            breakStart?.let { schedule.breakStart = it }
        }
        val check = schedule.run { validateSchedule(ScheduleDto(day, startTime, endTime, breakStart)) }
        if (!check) throw ScheduleDontCorrectException()
        schedule.breakEnd = schedule.breakStart.plusMinutes(60)
        return ScheduleMapper.toInfo(repository.save(schedule))
    }

    override fun delete(id: Long) {
        val schedule = repository.findByIdAndDoctor_UserAndDeletedFalse(id,getCurrentUser()) ?: throw ScheduleNotFoundException()
        if (appointmentRepository.existsByDoctorAndStartDate(schedule.doctor, schedule.day))
            throw ScheduleAlreadyAppointmentsException()
        schedule.deleted = true
        repository.save(schedule)
    }

    override fun findAll(pageable: Pageable): Page<ScheduleInfo> {
        return repository.findAllByDoctor_UserAndDeletedFalse(getCurrentUser(),pageable).map { ScheduleMapper.toInfo(it) }
    }

    override fun findById(id: Long): ScheduleInfo {
        return repository.findByIdAndDoctor_UserAndDeletedFalse(id,getCurrentUser())?.let{ScheduleMapper.toInfo(it) }?: throw ScheduleNotFoundException()
    }

    override fun showFreeTimes(doctorId: Long): Map<LocalDate,List<FreeTimeInfo>> {
        val schedules = repository.findAllByDoctor_IdAndDeletedFalse(doctorId)
        val list = mutableMapOf<LocalDate,List<FreeTimeInfo>>()
        schedules.forEach { schedule ->
            val appointments = appointmentRepository.findAllByDoctorAndStartDateAndStatusAndDeletedFalse(
                    schedule.doctor, schedule.day, AppointmentStatus.PENDING)
            list.put(schedule.day,getFreeTime(schedule,appointments))
        }
        return list
    }

    private fun validateSchedule(dto: ScheduleDto): Boolean {
        val today = LocalDate.now()
        dto.run {
            if (day.isAfter(today.plusDays(7))) return false
            if (startTime.isAfter(breakStart))return false
            if (breakStart.plusHours(1).isAfter(endTime)) return false
        }
        return true
    }
    private fun getFreeTime(schedule:Schedule, appointments: List<Appointment>):List<FreeTimeInfo>{
        val list= mutableListOf<FreeTimeInfo>()
        schedule.run {
            var from: LocalTime = startTime
            var till: LocalTime = breakStart
            for (appointment in appointments) {
                if (appointment.startTime.isAfter(breakEnd) && till == breakStart ) {
                    if (from != till) list.add(FreeTimeInfo(from, till))
                    from = breakEnd
                    till = endTime
                }
                list.add(FreeTimeInfo(from,appointment.startTime))
                from = appointment.startTime.plusMinutes(30)
            }
            if (from != till ) list.add(FreeTimeInfo(from,till))
            if (appointments.isEmpty() || till == breakStart) list.add(FreeTimeInfo(breakEnd,endTime))
        }
        return list
    }
}
@Service
class AuthServiceImp(private val userService: CustomUserDetailsService,
    private val jwtTokenUtils: JwtTokenUtils): AuthService{
    override fun generateToken(dto: AuthDto): TokenDto {
        val details = userService.loadUserByUsername(dto.username)
        val token = jwtTokenUtils.generateToken(details)
        val refreshToken = jwtTokenUtils.refreshToken(token)
        return TokenDto(token, refreshToken)
    }

    override fun generateTokenWithRefreshToken(dto: RefreshTokenDto): TokenDto {
        val token = jwtTokenUtils.generateTokenWithRefreshToken(dto.refreshToken)
        val refreshToken = jwtTokenUtils.refreshToken(token)
        return TokenDto(token, refreshToken)
    }
}
@Service
class DepartmentServiceImp(private val repository: DepartmentRepository): DepartmentService{
    override fun create(dto: DepartmentDto): DepartmentInfo {
        repository.findByNameAndClinic(dto.name,getCurrentUser().clinic!!)?.let { throw DepartmentAlreadyExistsException()}
        val department = repository.save(DepartmentMapper.toEntity(dto))
        return DepartmentMapper.toInfo(department)
    }

    override fun update(id: Long,dto: DepartmentUpdateDto): DepartmentInfo {
        repository.findByIdAndDeletedFalseForDirector(id)?.run{
            repository.findByNameAndClinic(dto.name,getCurrentUser().clinic!!)?.let { throw DepartmentAlreadyExistsException() }
            dto.name.let { name = it }
            return DepartmentMapper.toInfo(repository.save(this))
        }?: throw DepartmentNotFoundException()
    }

    override fun delete(id: Long) {
        repository.trashForDirector(id)
    }

    override fun findAll(pageable: Pageable): Page<DepartmentInfo> {
        return repository.findAllNotDeletedForDirector(pageable).map { DepartmentMapper.toInfo(it) }
    }

    override fun findById(id: Long): DepartmentInfo {
        return repository.findByIdAndDeletedFalseForDirector(id)?.let { DepartmentMapper.toInfo(it)}?:throw DepartmentNotFoundException()
    }

}
@Service
class ServicesImpl(private val repository: ServiceRepository,
    private val departmentRepo: DepartmentRepository): Servicess{
    override fun create(dto: ServiceDto): ServiceInfo {
        val department = departmentRepo.findByIdAndClinicAndDeletedFalse(dto.departmentId, getCurrentUser().clinic!!)?: throw DepartmentNotFoundException()
        repository.findByNameAndDepartment(dto.name, department)?.let{throw ServiceAlreadyExistsException() }
        val services = repository.save(ServiceMapper.toEntity(dto, department))
        return ServiceMapper.toInfo(services)
    }

    override fun update(id: Long, dto: ServiceUpdateDto): ServiceInfo {
        val services=repository.findByIdAndDeletedFalse(id)?.run{
            if ((department.clinic != getCurrentUser().clinic)) throw ServiceNotFoundException()
            this
        }?: throw ServiceNotFoundException()
        dto.run {
            name?.let { repository.findByNameAndDepartment(it,services.department)?.let{throw ServiceAlreadyExistsException()}
                services.name = it }
            description?.let{services.description = it}
            price?.let{services.price = it}
        }
        return ServiceMapper.toInfo(repository.save(services))
    }

    override fun delete(id: Long) {
        repository.findByIdAndDeletedFalse(id)?.run{
            if ((department.clinic != getCurrentUser().clinic)) throw ServiceNotFoundException()
            deleted = true
            repository.save(this)
        }?: throw ServiceNotFoundException()
    }

    override fun findAll(pageable: Pageable): Page<ServiceInfo> {
       return repository.findAllByClinic(getCurrentUser().clinic!!,pageable).map { ServiceMapper.toInfo(it) }
    }

    override fun findById(id: Long): ServiceInfo {
        return repository.findByIdAndDeletedFalse(id)?.let{ ServiceMapper.toInfo(it) }?: throw ServiceNotFoundException()
    }

}
@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String?): User {
        if(username.isNullOrBlank() || username.isBlank())
            throw UsernameInvalidException()
        return userRepository.findByUserNameAndDeletedFalse(username) ?: throw UserNotFoundException()
    }
}