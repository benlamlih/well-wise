package net.benlamlih.appointmentservice.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import net.benlamlih.appointmentservice.client.UserServiceClient;
import net.benlamlih.appointmentservice.dto.AppointmentResponse;
import net.benlamlih.appointmentservice.dto.mapper.AppointmentMapper;
import net.benlamlih.appointmentservice.model.Appointment;
import net.benlamlih.appointmentservice.model.AppointmentStatus;
import net.benlamlih.appointmentservice.model.CancellationMessage;
import net.benlamlih.appointmentservice.model.Payment;
import net.benlamlih.appointmentservice.model.PaymentMethod;
import net.benlamlih.appointmentservice.model.PaymentResult;
import net.benlamlih.appointmentservice.repository.AppointmentRepository;

@Service
public class AppointmentService {

	private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

	private final AppointmentRepository appointmentRepository;
	private final UserServiceClient userServiceClient;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	public AppointmentService(AppointmentRepository appointmentRepository, UserServiceClient userServiceClient,
			KafkaTemplate<String, Object> kafkaTemplate) {
		this.appointmentRepository = appointmentRepository;
		this.userServiceClient = userServiceClient;
		this.kafkaTemplate = kafkaTemplate;
	}

	public boolean bookAppointment(String doctorId, String patientId, LocalDate date, LocalTime startTime,
			LocalTime endTime, String serviceType, String details, Payment payment) {
		logger.info("Booking appointment for doctorId: {}, patientId: {}", doctorId, patientId);
		try {
			Appointment appointment = createAppointment(doctorId, patientId, date, startTime, endTime, serviceType,
					details, payment);

			userServiceClient.updateDoctorAvailability(doctorId, date, startTime, endTime, false);
			logger.info("Updated doctor availability for doctorId: {} to be set to false", doctorId);

			appointmentRepository.save(appointment);
			logger.info("Appointment saved with status: {}", appointment.getStatus());

			if (PaymentMethod.ONLINE.equals(payment.getMethod())) {
				sendPaymentRequest(payment);
			}

			return true;
		} catch (Exception e) {
			logger.error("Failed to book appointment: {}", e.getMessage());
			return false;
		}
	}

	private Appointment createAppointment(String doctorId, String patientId, LocalDate date, LocalTime startTime,
			LocalTime endTime, String serviceType, String details, Payment payment) {
		Date startDateTime = Date.from(startTime.atDate(date).atZone(ZoneId.systemDefault()).toInstant());
		Date endDateTime = Date.from(endTime.atDate(date).atZone(ZoneId.systemDefault()).toInstant());
		AppointmentStatus status = determineStatus(payment);

		return new Appointment.Builder().doctorId(doctorId).patientId(patientId).serviceType(serviceType)
				.dateTime(startDateTime).endDateTime(endDateTime).status(status).details(details).payment(payment)
				.build();
	}

	private void sendPaymentRequest(Payment payment) {
		kafkaTemplate.send("payment-request-topic", payment);
		logger.info("Online payment processed and sent to Kafka topic");
	}

	public boolean cancelAppointment(String appointmentId, String cancelledBy, String reason) {
		logger.info("Cancelling appointment with ID: {}", appointmentId);

		try {
			Appointment appointment = appointmentRepository.findById(appointmentId)
					.orElseThrow(() -> new IllegalStateException("Appointment not found with ID: " + appointmentId));

			appointment.setStatus(AppointmentStatus.CANCELLED);
			appointmentRepository.save(appointment);
			logger.info("Appointment status updated to CANCELLED for ID: {}", appointmentId);

			CancellationMessage cancellationMessage = new CancellationMessage(appointmentId, cancelledBy, Instant.now(),
					reason);
			sendCancellationRequest(cancellationMessage, appointmentId);

			String doctorId = appointment.getDoctorId();
			userServiceClient.updateDoctorAvailability(doctorId, appointment.getLocaleDate(),
					appointment.getStartTime(), appointment.getEndTime(), true);
			logger.info("Updated doctor availability for doctorId: {} to be set to true", doctorId);

			return true;
		} catch (Exception e) {
			logger.error("Error cancelling appointment ID {}: {}", appointmentId, e.getMessage());
			return false;
		}
	}

	private void sendCancellationRequest(CancellationMessage cancellationMessage, String appointmentId) {
		kafkaTemplate.send("cancellation-topic", cancellationMessage);
		logger.info("Cancellation message sent for Appointment ID: {}", appointmentId);
	}

	public boolean handlePaymentUpdate(PaymentResult paymentResult) {
		try {
			Appointment appointment = appointmentRepository.findById(paymentResult.getAppointmentId())
					.orElseThrow(() -> new IllegalStateException(
							"No appointment found with ID: " + paymentResult.getAppointmentId()));

			if (paymentResult.isSuccess()) {
				appointment.setStatus(AppointmentStatus.CONFIRMED);
				logger.info("Payment successful for appointment ID {}", paymentResult.getAppointmentId());
			} else {
				appointment.setStatus(AppointmentStatus.CANCELLED);
				logger.info("Payment failed for appointment ID {}, reason: {}", paymentResult.getAppointmentId(),
						paymentResult.getMessage());
			}

			appointmentRepository.save(appointment);
			return true;
		} catch (Exception e) {
			logger.error("Error updating payment status for appointment ID {}: {}", paymentResult.getAppointmentId(),
					e.getMessage());
			return false;
		}
	}

	private AppointmentStatus determineStatus(Payment payment) {
		return PaymentMethod.ONLINE.equals(payment.getMethod()) ? AppointmentStatus.PENDING
				: AppointmentStatus.CONFIRMED;
	}

	public List<AppointmentResponse> getAllAppointments() {
		List<Appointment> appointments = appointmentRepository.findAll();
		logger.info("Fetched all appointments");
		return appointments.stream().map(AppointmentMapper.INSTANCE::appointmentToAppointmentResponse)
				.collect(Collectors.toList());
	}
}
