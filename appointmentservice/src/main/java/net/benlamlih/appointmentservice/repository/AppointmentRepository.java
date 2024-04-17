package net.benlamlih.appointmentservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import net.benlamlih.appointmentservice.model.Appointment;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    @Query("{ 'dateTime' : { $gte: ?0, $lte: ?1 } }")
    List<Appointment> findAppointmentsBetween(LocalDateTime start, LocalDateTime end);
}
