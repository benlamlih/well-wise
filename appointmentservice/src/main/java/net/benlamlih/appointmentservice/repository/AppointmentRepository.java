package net.benlamlih.appointmentservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.benlamlih.appointmentservice.model.Appointment;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
}
