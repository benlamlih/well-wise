package net.benlamlih.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import net.benlamlih.appointmentservice.client.UserServiceClient;
import net.benlamlih.appointmentservice.model.Appointment;
import net.benlamlih.appointmentservice.model.AppointmentStatus;
import net.benlamlih.appointmentservice.model.CancellationMessage;
import net.benlamlih.appointmentservice.model.Payment;
import net.benlamlih.appointmentservice.model.PaymentMethod;
import net.benlamlih.appointmentservice.repository.AppointmentRepository;
import net.benlamlih.appointmentservice.service.AppointmentService;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

	@Mock
	private AppointmentRepository appointmentRepository;

	@Mock
	private UserServiceClient userServiceClient;

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@InjectMocks
	private AppointmentService appointmentService;

	@Captor
	private ArgumentCaptor<Appointment> appointmentCaptor;

	@Captor
	private ArgumentCaptor<CancellationMessage> cancellationMessageCaptor;

	@Test
	void bookPhysicalAppointment() {
		String doctorId = "1";
		String patientId = "2";
		LocalDate date = LocalDate.now();
		LocalTime startTime = LocalTime.of(10, 0);
		LocalTime endTime = LocalTime.of(11, 0);
		String serviceType = "General Checkup";
		String details = "Details";
		Payment payment = new Payment();
		payment.setMethod(PaymentMethod.PHYSICAL);

		appointmentService.bookAppointment(doctorId, patientId, date, startTime, endTime, serviceType, details,
				payment);

		verify(appointmentRepository).save(any());
		verify(userServiceClient).updateDoctorAvailability(doctorId, date, startTime, endTime, false);
		verify(kafkaTemplate, never()).send(anyString(), any());
	}

	@Test
	void bookOnlineAppointment() {
		String doctorId = "1";
		String patientId = "2";
		LocalDate date = LocalDate.now();
		LocalTime startTime = LocalTime.of(10, 0);
		LocalTime endTime = LocalTime.of(11, 0);
		String serviceType = "General Checkup";
		String details = "Details";
		Payment payment = new Payment();
		payment.setMethod(PaymentMethod.ONLINE);

		appointmentService.bookAppointment(doctorId, patientId, date, startTime, endTime, serviceType, details,
				payment);

		verify(appointmentRepository).save(any());
		verify(userServiceClient).updateDoctorAvailability(doctorId, date, startTime, endTime, false);
		verify(kafkaTemplate).send(eq("payment-request-topic"), any(Payment.class));
	}

	@Test
	void cancelAppointment() {
		String appointmentId = "123";
		String cancelledBy = "Patient";
		String reason = "Changed plans";
		Appointment appointment = new Appointment();
		appointment.setId(appointmentId);
		appointment.setStatus(AppointmentStatus.CONFIRMED);

		when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

		appointmentService.cancelAppointment(appointmentId, cancelledBy, reason);

		verify(appointmentRepository).save(appointmentCaptor.capture());
		assertEquals(AppointmentStatus.CANCELLED, appointmentCaptor.getValue().getStatus());
		verify(kafkaTemplate).send(eq("cancellation-topic"), cancellationMessageCaptor.capture());
	}
}
