package net.benlamlih.appointmentservice.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.benlamlih.appointmentservice.client.UserServiceClient;
import net.benlamlih.appointmentservice.model.Appointment;
import net.benlamlih.appointmentservice.model.AppointmentStatus;
import net.benlamlih.appointmentservice.model.Payment;
import net.benlamlih.appointmentservice.repository.AppointmentRepository;

@Service
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final UserServiceClient userServiceClient;

	@Autowired
	public AppointmentService(AppointmentRepository appointmentRepository, UserServiceClient userServiceClient) {
		this.appointmentRepository = appointmentRepository;
		this.userServiceClient = userServiceClient;
	}

	public boolean bookAppointment(String doctorId, String patientId, LocalDate date, LocalTime startTime,
			LocalTime endTime, String serviceType, AppointmentStatus status, String details, Payment payment) {

		Date startDateTime = Date.from(startTime.atDate(date).atZone(ZoneId.systemDefault()).toInstant());
		Date endDateTime = Date.from(endTime.atDate(date).atZone(ZoneId.systemDefault()).toInstant());

		Appointment appointment = new Appointment.Builder().doctorId(doctorId).patientId(patientId)
				.serviceType(serviceType).dateTime(startDateTime).endDateTime(endDateTime).status(status)
				.details(details).payment(payment).build();

		appointmentRepository.save(appointment);

		userServiceClient.updateDoctorAvailability(doctorId, date, startTime, endTime);

		return true;
	}

	public boolean cancelAppointment(String appointmentId) {
		// TODO: Implement cancellation logic
		return true;
	}

	public boolean confirmAppointment(String appointmentId) {
		// TODO: Implement confirmation logic
		return true;
	}
}
