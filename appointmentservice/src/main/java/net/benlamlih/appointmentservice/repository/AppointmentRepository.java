package net.benlamlih.appointmentservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.benlamlih.appointmentservice.model.Appointment;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {

	List<Appointment> findAppointmentsBetween(LocalDateTime now, LocalDateTime oneDayAhead);
}
