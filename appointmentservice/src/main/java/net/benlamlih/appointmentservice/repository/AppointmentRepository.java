package net.benlamlih.appointmentservice.repository;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.benlamlih.appointmentservice.model.Appointment;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
	boolean existsByPatientIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(String patientId, LocalDate date,
			LocalTime startTime, LocalTime endTime);

	boolean existsByDoctorIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(String doctorId, LocalDate date,
			LocalTime startTime, LocalTime endTime);
}
