package net.benlamlih.appointmentservice.service;

import static net.benlamlih.appointmentservice.util.DateTimeUtil.toDate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.benlamlih.appointmentservice.client.UserServiceClient;
import net.benlamlih.appointmentservice.dto.AppointmentRequest;
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
@Transactional
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final UserServiceClient userServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AppointmentService(AppointmentRepository appointmentRepository, UserServiceClient userServiceClient,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.appointmentRepository = appointmentRepository;
        this.userServiceClient = userServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public boolean bookAppointment(AppointmentRequest request) {
        String doctorId = request.getDoctorId();
        String patientId = request.getPatientId();

        logger.info("Booking appointment for doctorId: {}, patientId: {}", doctorId, patientId);
        try {
            Appointment appointment = createAppointment(request);

            LocalDate date = request.getDate();
            LocalTime startTime = request.getStartTime();
            LocalTime endTime = request.getEndTime();
            userServiceClient.updateDoctorAvailability(doctorId, date, startTime, endTime, false);
            logger.info("Updated doctor availability for doctorId: {} to be set to false", doctorId);

            appointmentRepository.save(appointment);
            logger.info("Appointment saved with status: {}", appointment.getStatus());

            Payment payment = request.getPayment();
            if (PaymentMethod.ONLINE.equals(payment.getMethod())) {
                sendPaymentRequest(payment);
            }

            return true;
        } catch (Exception e) {
            logger.error("Failed to book appointment for doctorId: {}, patientId: {}, error: {}", doctorId, patientId,
                    e.getMessage());
            return false;
        }
    }

    private Appointment createAppointment(AppointmentRequest request) {
        Date startDateTime = toDate(request.getStartTime(), request.getDate());
        Date endDateTime = toDate(request.getEndTime(), request.getDate());

        AppointmentStatus status = determineStatus(request.getPayment());

        return new Appointment.Builder()
                .doctorId(request.getDoctorId())
                .patientId(request.getPatientId())
                .serviceType(request.getServiceType())
                .dateTime(startDateTime)
                .endDateTime(endDateTime)
                .status(status)
                .details(request.getDetails())
                .payment(request.getPayment())
                .build();
    }

    private void sendPaymentRequest(Payment payment) {
        kafkaTemplate.send("payment-request-topic", payment);
        logger.info("Online payment processed and sent to Kafka topic");
    }

    public boolean cancelAppointment(String appointmentId, String cancelledBy, String reason) {
        logger.info("Cancelling appointment with ID: {}", appointmentId);

        try {
            Appointment appointment = fetchAppointment(appointmentId);

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
            Appointment appointment = fetchAppointment(paymentResult.getAppointmentId());

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

    private Appointment fetchAppointment(String appointmentId) {
        return appointmentRepository.findById(appointmentId).orElseThrow(() -> new IllegalStateException(
                "No appointment found with ID: " + appointmentId));
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
