package net.benlamlih.appointmentservice.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.benlamlih.appointmentservice.model.Availability;

public interface AvailabilityRepository extends MongoRepository<Availability, String> {

	Optional<Availability> findByDoctorIdAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(String doctorId,
			LocalDate date, LocalTime startTime, LocalTime endTime);
}
