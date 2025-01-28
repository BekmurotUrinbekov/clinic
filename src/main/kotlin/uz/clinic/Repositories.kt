package uz.clinic

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.JoinType
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date


@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalseForDoctor(id: Long): T?
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trashForDoctor(id: Long): T?
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): List<T>
    fun findAllNotDeletedForPageableForDoctor(pageable: Pageable): Page<T>
    fun saveAndRefresh(t: T): T
    fun findAllByDoctorNotDeleted(): List<T>
    fun findAllNotDeletedForDirector(pageable: Pageable): Page<T>
    fun trashForDirector(id: Long): T?
    fun findByIdAndDeletedFalseForDirector(id: Long):T?
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    private val entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }
    val clinicForDoctor = Specification<T> { root, _, cb ->
        val employeeJoin: Join<T, Employee> = root.join("doctor", JoinType.INNER)
        val userJoin: Join<Employee, User> = employeeJoin.join("user", JoinType.INNER)
        cb.and(
            cb.equal(root.get<Boolean>("deleted"), false),
            cb.equal(userJoin.get<Clinic>("clinic"), getCurrentUser().clinic!!)
        )
    }
    val clinicForDirector = Specification<T> { root, _, cb ->
        val clinicJoin = root.join<T, Clinic>("clinic", JoinType.INNER)
        cb.and(
            cb.equal(root.get<Boolean>("deleted"), false),
            cb.equal(clinicJoin, getCurrentUser().clinic!!),
            cb.equal(clinicJoin.get<Boolean>("deleted"), false)
        )
    }
    val byId: (Long) -> Specification<T> = { id ->
        Specification<T> { root, _, cb ->
            cb.equal(root.get<Long>("id"), id)
        }
    }

    override fun findByIdAndDeletedFalseForDoctor(id: Long):T?{
        val findOne = findOne(byId(id).and(clinicForDoctor))
        if (findOne.isPresent) return findOne.get()
        return null
    }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }


    override fun findByIdAndDeletedFalseForDirector(id: Long):T?{
        val findOne = findOne(byId(id).and(clinicForDirector))
        if (findOne.isPresent) return findOne.get()
        return null
    }

    @Transactional
    override fun trashForDoctor(id: Long): T? {
        val findOne = findOne(byId(id).and(clinicForDoctor))
        if (findOne.isPresent) {
            val get = findOne.get()
            get.deleted = true
            return save(get)
        }
        return null
    }
    @Transactional
    override fun trashForDirector(id: Long): T? {
        val findOne = findOne(byId(id).and(clinicForDirector))
        if (findOne.isPresent) {
            val get = findOne.get()
            get.deleted = true
            return save(get)
        }
        return null
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllByDoctorNotDeleted(): List<T> = findAll(clinicForDoctor)
    override fun findAllNotDeletedForDirector(pageable: Pageable): Page<T> = findAll(clinicForDirector , pageable)
    override fun findAllNotDeleted(pageable: Pageable): List<T> = findAll(pageable).content
    override fun findAllNotDeletedForPageableForDoctor(pageable: Pageable): Page<T> =
        findAll(clinicForDoctor, pageable)


    @Transactional
    override fun saveAndRefresh(t: T): T {
        return save(t).apply { entityManager.refresh(this) }
    }
}

interface UserRepository: BaseRepository<User>{
    fun findByUserNameAndDeletedFalse(username: String): User?
    fun existsByUserNameOrPhoneNumber(username: String, phoneNumber: String): Boolean
    fun findByUserName(username: String): User?
    fun findByPhoneNumber(phoneNumber: String): User?
}
interface ClinicRepository: BaseRepository<Clinic>{
//    fun existsByNameAndAddressAndPhoneNumberAndEmail(name: String, address: String, phoneNumber: String,email: String): Boolean
@Query(
    """
    SELECT c 
    FROM Clinic c 
    WHERE c.deleted = false
"""
)
fun findAllPageableAndDeletedFalse(pageable: Pageable): Page<Clinic>

    fun existsByNameOrAddressOrPhoneNumberOrEmail(name: String?, address: String?, phoneNumber: String?, email: String?): Boolean

}
interface DepartmentRepository : BaseRepository<Department>{
    fun findByNameAndClinic(name: String, clinic: Clinic): Department?
    fun findByIdAndClinicAndDeletedFalse(id: Long, clinic: Clinic): Department?
}
interface EmployeeRepository: BaseRepository<Employee>{
    @Query("""
    SELECT e 
    FROM Employee e 
    INNER JOIN e.user u 
    WHERE u.clinic = :clinic AND u.deleted = false AND e.deleted = false
""")
    fun findAllByClinic(@Param("clinic") clinic: Clinic, pageable: Pageable): Page<Employee>

    @Query("""
    SELECT e 
    FROM Employee e
    WHERE e.id = :id AND e.user.clinic = :clinic AND e.user.deleted = false AND e.deleted = false
    """)
    fun findByIdAndClinic(@Param("id") id: Long, @Param("clinic") clinic: Clinic): Employee?
    fun findByUserAndDeletedFalse(user: User): Employee?

}
interface ServiceRepository : BaseRepository<Services>{
    fun findByNameAndDepartment(name: String, department: Department): Services?
    @Query("""
        select s from Services s Inner Join s.department d 
        where d.clinic = :clinic And s.deleted = false 
        And d.deleted = false 
    """)
    fun findAllByClinic(@Param("clinic")clinic: Clinic,pageable: Pageable): Page<Services>
    fun findByIdAndDepartment_ClinicAndDeletedFalse(id: Long, clinic: Clinic): Services?
}
interface ScheduleRepository : BaseRepository<Schedule>{
    fun findByDoctorAndDayAndDeletedFalse(doctor: Employee,day: LocalDate): Schedule?
    fun findAllByDoctor_UserAndDeletedFalse(user: User,pageable: Pageable): Page<Schedule>
    fun findByIdAndDoctor_UserAndDeletedFalse(id: Long,user: User) : Schedule?
    fun findAllByDoctor_IdAndDeletedFalse(doctorId:Long): List<Schedule>
}
interface AppointmentRepository : BaseRepository<Appointment>{
    fun existsByDoctorAndStartDate(doctor: Employee,startDate: LocalDate): Boolean
    @Query("""
    SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
    FROM Appointment a
    WHERE (a.doctor = :doctor
           AND a.startDate = :startDate
           AND a.deleted = false
           AND a.startTime BETWEEN :timeStart AND :timeEnd)
       OR (a.doctor = :doctor 
           AND a.patient = :patient 
           AND a.startDate = :startDate
           AND a.deleted = false)
""")
    fun existsByDoctorAndStartDateAndTimeRange(
        @Param("doctor") doctor: Employee,
        @Param("startDate") startDate: LocalDate,
        @Param("timeStart") timeStart: LocalTime,
        @Param("timeEnd") timeEnd: LocalTime,
        @Param("patient") patient: User
    ): Boolean

    fun findByIdAndPatientAndDeletedFalse(id:Long,patient: User ): Appointment?
    fun findByIdAndPatientAndStatusAndDeletedFalse(id: Long, patient: User, status: AppointmentStatus): Appointment?
    fun findAllByPatientAndStatusAndDeletedFalse(patient: User, status: AppointmentStatus,pageable: Pageable): Page<Appointment>
    fun findAllByDoctor_UserAndStatusAndDeletedFalse(user: User, status: AppointmentStatus, pageable: Pageable): Page<Appointment>
    fun findAllByDoctorAndStartDateAndStatusAndDeletedFalse(doctor: Employee,startDate: LocalDate,status: AppointmentStatus,sort: Sort = Sort.by(Sort.Direction.ASC, "startTime")):List<Appointment>
}
interface TransactionRepository : BaseRepository<Transaction>{
    fun findAllByDoctorIsNullAndServices_Department_Clinic(clinic:Clinic,pageable: Pageable): Page<Transaction>
    fun findAllByServicesIsNullAndDoctor_User_Clinic(clinic: Clinic,pageable: Pageable): Page<Transaction>
}
interface DiagnosisAnalysisRepository: BaseRepository<DiagnosisAnalysis>{
    fun findAllByPatientIdAndDoctor_User_ClinicAndDeletedFalse(patientId: Long,clinic: Clinic): List<DiagnosisAnalysis>
    @Query("""
    SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
    FROM DiagnosisAnalysis d
    WHERE d.patient = :patient
      AND d.doctor.user = :user
      AND FUNCTION('DATE', d.createdAt) = FUNCTION('DATE', :date)
      AND d.deleted = false
""")
    fun existsByPatientAndDoctorAndCreatedAt_DateAndDeletedFalse(
        @Param("patient") patient: User,
        @Param("user") user: User,
        @Param("date") date: Date
    ): Boolean

}

