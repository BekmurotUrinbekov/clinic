package uz.clinic

class UserMapper{
    companion object{
        fun toEntity(dto: UserDto,role: UserRole?,clinic: Clinic?): User {
            return role?.let { dto.run { User(username, password, fullName,gender,address,phoneNumber,birthDate, role, clinic) } }
                ?: dto.run { User(username, password, fullName,gender,address,phoneNumber,birthDate, clinic = clinic) }
        }
        fun toInfo(user: User): UserInfo {
            return user.run { UserInfo(id!!,userName,fullName,role,gender,address,phoneNumber,birthDate) }
        }
    }
}
class EmployeeMapper{
    companion object{
        fun toEntity(dto: EmployeeDto,user: User): Employee {
            return dto.run { Employee(experience,education,user,consultantPrice) }
        }
        fun toInfo(employee: Employee): EmployeeInfo {
            return employee.run { EmployeeInfo(id!!,UserMapper.toInfo(user),experience,education) }
        }
    }
}
class ClinicMapper {
    companion object {
        fun toEntity(dto: ClinicDto): Clinic {
            return dto.run { Clinic(name, address, phoneNumber, email) }
        }
        fun toInfo(clinic: Clinic):ClinicInfo {
            return clinic.run { ClinicInfo(id!!, name, address, phoneNumber, email) }
        }
    }
}
class ScheduleMapper{
    companion object{
        fun toEntity(dto: ScheduleDto,doctor: Employee): Schedule {
            return dto.run{Schedule(doctor,day,startTime,endTime,breakStart, breakStart.plusMinutes(60))}
        }
        fun toInfo(schedule: Schedule): ScheduleInfo {
            return schedule.run { ScheduleInfo(id!!,EmployeeMapper.toInfo(doctor),day,startTime,endTime,breakStart,breakEnd) }
        }
    }
}
class AppointmentMapper{
    companion object{
        fun toEntity(dto: AppointmentDto,doctor: Employee): Appointment{
            return dto.run { Appointment(getCurrentUser(),doctor,startDate,startTime) }
        }
        fun toInfo(appointment: Appointment): AppointmentInfo{
            return appointment.run { AppointmentInfo(id!!,patient.fullName, EmployeeMapper.toInfo(doctor),startDate,startTime,status) }
        }
    }
}
class ServiceMapper{
    companion object{
        fun toEntity(dto: ServiceDto,department: Department): Services{
            return dto.run { Services(name,description,price,department)}
        }
        fun toInfo(services: Services): ServiceInfo{
            return services.run { ServiceInfo(id!!,name,description,price, DepartmentMapper.toInfo(department)) }
        }
    }
}
class DepartmentMapper{
    companion object{
        fun toEntity(dto: DepartmentDto): Department {
            return Department(dto.name,getCurrentUser().clinic!!)
        }
        fun toInfo(department: Department): DepartmentInfo {
            return department.run { DepartmentInfo(id!!,name,  ClinicMapper.toInfo(clinic))}
        }
    }
}
class TransactionMapper{
    companion object{
        fun toInfo(transaction: Transaction): TransactionInfo {
            return transaction.run{TransactionInfo(id!!,patient.fullName,amount,paymentMethod,doctor?.user?.fullName,services?.name)}
        }
    }
}
class DiagnosisAnalysisMapper{
    companion object{
        fun toInfo(diagnosisAnalysis:DiagnosisAnalysis):DiagnosisAnalysisInfo{
            return diagnosisAnalysis.run{ DiagnosisAnalysisInfo(id!!,patient.fullName,doctor.user.fullName,type,result)}
        }
    }
}
