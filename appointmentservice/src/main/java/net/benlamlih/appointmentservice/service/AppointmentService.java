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
        logger.info("Booking appointment for doctorId: {}, patientId: {}", request.getDoctorId(),
                request.getPatientId());
        try {
            Appointment appointment = createAppointment(request);
            updateDoctorAvailability(appointment.getDoctorId(), request.getDate(),
                    request.getStartTime(), request.getEndTime(), false);
            appointmentRepository.save(appointment);
            logger.info("Appointment booked and saved with status: {}", appointment.getStatus());
            handlePayment(request.getPayment());
            return true;
        } catch (Exception e) {
            logger.error("Failed to book appointment: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean rescheduleAppointment(String appointmentId, AppointmentRequest newRequest) {
        logger.info("Rescheduling appointment with ID: {}", appointmentId);
        try {
            Appointment appointment = fetchAppointment(appointmentId);
            freeUpDoctorAvailability(appointment);
            updateAppointmentDetails(appointment, newRequest);
            updateDoctorAvailability(appointment.getDoctorId(), newRequest.getDate(),
                    newRequest.getStartTime(), newRequest.getEndTime(), false);
            appointmentRepository.save(appointment);
            logger.info("Appointment rescheduled successfully for ID: {}", appointmentId);
            handlePayment(newRequest.getPayment());
            return true;
        } catch (Exception e) {
            logger.error("Failed to reschedule appointment ID {}: {}", appointmentId, e.getMessage());
            return false;
        }
    }

    public boolean cancelAppointment(String appointmentId, String cancelledBy, String reason) {
        logger.info("Cancelling appointment with ID: {}", appointmentId);
        try {
            Appointment appointment = fetchAppointment(appointmentId);
            freeUpDoctorAvailability(appointment);
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
            sendCancellationRequest(new CancellationMessage(appointmentId, cancelledBy, Instant.now(), reason));
            return true;
        } catch (Exception e) {
            logger.error("Failed to cancel appointment ID {}: {}", appointmentId, e.getMessage());
            return false;
        }
    }

    private void freeUpDoctorAvailability(Appointment appointment) {
        userServiceClient.updateDoctorAvailability(appointment.getDoctorId(), appointment.getLocaleDate(),
                appointment.getStartTime(), appointment.getEndTime(), true);
    }

    private void updateAppointmentDetails(Appointment appointment, AppointmentRequest newRequest) {
        appointment.setDateTime(toDate(newRequest.getStartTime(), newRequest.getDate()));
        appointment.setEndDateTime(toDate(newRequest.getEndTime(), newRequest.getDate()));
        appointment.setServiceType(newRequest.getServiceType());
        appointment.setDetails(newRequest.getDetails());
        appointment.setPayment(newRequest.getPayment());
        appointment.setStatus(determineStatus(newRequest.getPayment()));
    }

    private Appointment fetchAppointment(String appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalStateException("No appointment found with ID: " + appointmentId));
    }

    private void handlePayment(Payment payment) {
        if (PaymentMethod.ONLINE.equals(payment.getMethod())) {
            sendPaymentRequest(payment);
        }
    }

    private void sendPaymentRequest(Payment payment) {
        kafkaTemplate.send("payment-request-topic", payment);
        logger.info("Payment request sent for payment method online.");
    }

    private void sendCancellationRequest(CancellationMessage cancellationMessage) {
        kafkaTemplate.send("cancellation-topic", cancellationMessage);
        logger.info("Cancellation message sent for Appointment ID: {}", cancellationMessage.getAppointmentId());
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

    private void updateDoctorAvailability(String doctorId, LocalDate date, LocalTime startTime, LocalTime endTime,
            boolean available) {
        userServiceClient.updateDoctorAvailability(doctorId, date, startTime, endTime, available);
        logger.info("Doctor availability updated for doctorId: {} to {}", doctorId, available ? "free" : "busy");
    }

    private AppointmentStatus determineStatus(Payment payment) {
        return PaymentMethod.ONLINE.equals(payment.getMethod()) ? AppointmentStatus.PENDING
                : AppointmentStatus.CONFIRMED;
    }

    public List<AppointmentResponse> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        return appointments.stream().map(AppointmentMapper.INSTANCE::appointmentToAppointmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Handles the payment result and updates the appointment status accordingly.
     * Callback method for payment service.
     * 
     * @param paymentResult Payment result
     * @return true if the payment status was updated successfully, false otherwise
     */
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
}
