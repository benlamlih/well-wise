package net.benlamlih.appointmentservice.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;

import net.benlamlih.appointmentservice.model.Appointment;
import net.benlamlih.appointmentservice.repository.AppointmentRepository;
import net.benlamlih.appointmentservice.repository.AvailabilityRepository;

public class AppointmentService {

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private AvailabilityRepository availabilityRepository;

	public boolean createAppointment(String doctorId, String patientId, LocalDate date, LocalTime startTime,
			LocalTime endTime) {

		if (!possibleAppointment(doctorId, date, startTime, endTime)) {
			return false;
		}

		Appointment appointment = new Appointment();
		appointment.setDoctorId(doctorId);
		appointment.setPatientId(patientId);
		appointment.setDate(date);
		appointment.setStartTime(startTime);
		appointment.setEndTime(endTime);
		appointment.setStatus("PENDING");
		appointmentRepository.save(appointment);

		return true;
	}

	public boolean cancelAppointment(String appointmentId) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));
		appointment.setStatus("CANCELLED");
		appointmentRepository.delete(appointment);
		return true;
	}

	public boolean confirmAppointment(String appointmentId) {
		Appointment appointment = appointmentRepository.findById(appointmentId).get();
		appointment.setStatus("CONFIRMED");
		appointmentRepository.save(appointment);
		return true;
	}

	public boolean rescuduleAppointment(String appointmentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
		Appointment appointment = appointmentRepository.findById(appointmentId).get();

		if (!possibleAppointment(appointment.getDoctorId(), date, startTime, endTime)) {
			return false;
		}

		appointment.setDate(date);
		appointment.setStartTime(startTime);
		appointment.setEndTime(endTime);
		appointmentRepository.save(appointment);
		return true;
	}

	public boolean possibleAppointment(String doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
		boolean isDoctorAvailable = availabilityRepository
				.findByDoctorIdAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(doctorId, date, startTime,
						endTime)
				.get().isAvailable();

		if (!isDoctorAvailable) {
			return false;
		}

		boolean doctorOverlap = appointmentRepository
				.existsByDoctorIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(doctorId, date, startTime,
						endTime);

		if (doctorOverlap) {
			return false;
		}

		return true;
	}

}
