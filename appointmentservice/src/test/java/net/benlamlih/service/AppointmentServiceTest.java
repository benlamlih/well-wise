package net.benlamlih.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.benlamlih.appointmentservice.client.UserServiceClient;
import net.benlamlih.appointmentservice.model.Appointment;
import net.benlamlih.appointmentservice.model.AppointmentStatus;
import net.benlamlih.appointmentservice.model.Payment;
import net.benlamlih.appointmentservice.repository.AppointmentRepository;
import net.benlamlih.appointmentservice.service.AppointmentService;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

	@Mock
	private AppointmentRepository appointmentRepository;

	@Mock
	private UserServiceClient userServiceClient;

	@InjectMocks
	private AppointmentService appointmentService;

	@Test
	void bookAppointment() {
		String doctorId = "1";
		String patientId = "2";
		LocalDate date = LocalDate.now();
		LocalTime startTime = LocalTime.of(10, 0);
		LocalTime endTime = LocalTime.of(11, 0);
		String serviceType = "General Checkup";
		AppointmentStatus status = AppointmentStatus.PENDING;
		String details = "Details";
		Payment payment = new Payment();

		appointmentService.bookAppointment(doctorId, patientId, date, startTime, endTime, serviceType, status, details,
				payment);

		verify(appointmentRepository).save(any(Appointment.class));
		verify(userServiceClient).updateDoctorAvailability(doctorId, date, startTime, endTime);
	}
}
