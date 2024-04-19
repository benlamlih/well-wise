package net.benlamlih.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import net.benlamlih.appointmentservice.client.UserServiceClient;
import net.benlamlih.appointmentservice.dto.AppointmentRequest;
import net.benlamlih.appointmentservice.model.Appointment;
import net.benlamlih.appointmentservice.model.AppointmentStatus;
import net.benlamlih.appointmentservice.model.CancellationMessage;
import net.benlamlih.appointmentservice.model.NotificationEvent;
import net.benlamlih.appointmentservice.model.Payment;
import net.benlamlih.appointmentservice.model.PaymentMethod;
import net.benlamlih.appointmentservice.model.PaymentResult;
import net.benlamlih.appointmentservice.repository.AppointmentRepository;
import net.benlamlih.appointmentservice.service.AppointmentService;
import net.benlamlih.appointmentservice.util.DateTimeUtil;

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

    private Payment onlinePayment;
    private Payment physicalPayment;
    private AppointmentRequest onlineRequest;
    private AppointmentRequest physicalRequest;

    private static final String DOCTOR_ID_123 = "doctor123";
    private static final String DOCTOR_ID_124 = "doctor124";

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        this.onlinePayment = new Payment();
        this.onlinePayment.setMethod(PaymentMethod.ONLINE);

        this.physicalPayment = new Payment();
        this.physicalPayment.setMethod(PaymentMethod.PHYSICAL);

        this.onlineRequest = createAppointmentRequest(DOCTOR_ID_123, "patient123", today, now, onlinePayment);
        this.physicalRequest = createAppointmentRequest(DOCTOR_ID_124, "patient124", today, now, physicalPayment);
    }

    private AppointmentRequest createAppointmentRequest(String doctorId, String patientId, LocalDate date,
            LocalTime startTime, Payment payment) {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(doctorId);
        request.setPatientId(patientId);
        request.setDate(date);
        request.setStartTime(startTime);
        request.setEndTime(startTime.plusHours(1));
        request.setPayment(payment);
        return request;
    }

    @Test
    void testBookAppointmentSuccess() {
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(new Appointment());
        boolean result = appointmentService.bookAppointment(onlineRequest);

        verifyBookingInteractions(DOCTOR_ID_123, true, true);
        assertTrue(result);
    }

    @Test
    void testPhysicalPaymentBookAppointmentSuccess() {
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(new Appointment());
        boolean result = appointmentService.bookAppointment(physicalRequest);

        verifyBookingInteractions(DOCTOR_ID_124, false, true);
        assertTrue(result);
    }

    private void verifyBookingInteractions(String doctorId, boolean expectPayment, boolean expectNotification) {
        verify(appointmentRepository).save(any(Appointment.class));
        verify(userServiceClient).updateDoctorAvailability(eq(doctorId), any(LocalDate.class), any(LocalTime.class),
                any(LocalTime.class), eq(false));
        int paymentTimes = expectPayment ? 1 : 0;
        int notificationTimes = expectNotification ? 1 : 0;
        verify(kafkaTemplate, times(paymentTimes)).send(eq("payment-request-topic"), any(Payment.class));
        verify(kafkaTemplate, times(notificationTimes)).send(eq("notification-topic"), any(NotificationEvent.class));
    }

    @Test
    void testBookAppointmentFailureOnSave() {
        doThrow(new RuntimeException("DB error")).when(appointmentRepository).save(any(Appointment.class));
        boolean result = appointmentService.bookAppointment(onlineRequest);

        verify(appointmentRepository).save(any(Appointment.class));
        verify(kafkaTemplate, never()).send(eq("notification-topic"), any(NotificationEvent.class));
        assertFalse(result);
    }

    @Test
    void testBookAppointmentFailureOnService() {
        doThrow(new RuntimeException("Service unavailable")).when(userServiceClient)
                .updateDoctorAvailability(eq(DOCTOR_ID_123), any(LocalDate.class), any(LocalTime.class),
                        any(LocalTime.class), anyBoolean());

        boolean result = appointmentService.bookAppointment(onlineRequest);

        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(kafkaTemplate, never()).send(eq("notification-topic"), any(NotificationEvent.class));
        assertFalse(result);
    }

    @Test
    void testRescheduleAppointmentSuccess() {
        String appointmentId = "123";
        setupExistingAppointment(appointmentId, DOCTOR_ID_123);

        LocalDate newDate = LocalDate.now().plusDays(1);
        LocalTime newStartTime = LocalTime.of(10, 0);
        LocalTime newEndTime = LocalTime.of(11, 0);

        AppointmentRequest newRequest = createAppointmentRequest(DOCTOR_ID_123, "patient123", newDate, newStartTime,
                onlinePayment);
        newRequest.setEndTime(newEndTime);

        doNothing().when(userServiceClient).updateDoctorAvailability(eq(DOCTOR_ID_123), any(LocalDate.class),
                any(LocalTime.class), any(LocalTime.class), eq(true));
        doNothing().when(userServiceClient).updateDoctorAvailability(eq(DOCTOR_ID_123), eq(newDate), eq(newStartTime),
                eq(newEndTime), eq(false));

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = appointmentService.rescheduleAppointment(appointmentId, newRequest);

        verify(userServiceClient).updateDoctorAvailability(eq(DOCTOR_ID_123), any(LocalDate.class),
                any(LocalTime.class),
                any(LocalTime.class), eq(true));
        verify(userServiceClient).updateDoctorAvailability(eq(DOCTOR_ID_123), eq(newDate), eq(newStartTime),
                eq(newEndTime),
                eq(false));
        verify(appointmentRepository).save(appointmentCaptor.capture());

        Appointment updatedAppointment = appointmentCaptor.getValue();
        assertNotNull(updatedAppointment);
        assertEquals(newDate, updatedAppointment.getLocaleDate());
        assertEquals(newStartTime, updatedAppointment.getStartTime());
        assertEquals(newEndTime, updatedAppointment.getEndTime());

        verify(kafkaTemplate, times(PaymentMethod.ONLINE.equals(newRequest.getPayment().getMethod()) ? 1 : 0))
                .send(eq("payment-request-topic"), any(Payment.class));

        verify(kafkaTemplate, times(1)).send(eq("notification-topic"), any(NotificationEvent.class));
        assertTrue(result);
    }

    @Test
    void cancelAppointment() {
        String appointmentId = "123";
        String cancelledBy = "Patient";
        String reason = "Changed plans";
        setupExistingAppointment(appointmentId, DOCTOR_ID_123);

        boolean result = appointmentService.cancelAppointment(appointmentId, cancelledBy, reason);

        verify(appointmentRepository).save(appointmentCaptor.capture());
        assertEquals(AppointmentStatus.CANCELLED, appointmentCaptor.getValue().getStatus());
        verify(kafkaTemplate).send(eq("cancellation-topic"), cancellationMessageCaptor.capture());
        verify(kafkaTemplate, times(1)).send(eq("notification-topic"), any(NotificationEvent.class));
        assertTrue(result);
    }

    private Appointment setupExistingAppointment(String appointmentId, String doctorId) {
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setDoctorId(doctorId);
        appointment.setPatientId("patient123");
        appointment.setDateTime(DateTimeUtil.toDate(LocalTime.now(), LocalDate.now()));
        appointment.setEndDateTime(DateTimeUtil.toDate(LocalTime.now().plusHours(1), LocalDate.now()));
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        return appointment;
    }

    @Test
    void testHandlePaymentUpdateSuccess() {
        String appointmentId = "123";
        setupExistingAppointment(appointmentId, DOCTOR_ID_123);
        PaymentResult paymentResult = createPaymentResult(appointmentId, true);

        boolean result = appointmentService.handlePaymentUpdate(paymentResult);

        verify(appointmentRepository).save(appointmentCaptor.capture());
        Appointment updatedAppointment = appointmentCaptor.getValue();
        assertNotNull(updatedAppointment);
        assertEquals(AppointmentStatus.CONFIRMED, updatedAppointment.getStatus());
        assertTrue(result);
    }

    @Test
    void testHandlePaymentUpdateFailure() {
        String appointmentId = "123";
        setupExistingAppointment(appointmentId, DOCTOR_ID_123);
        PaymentResult paymentResult = createPaymentResult(appointmentId, false);

        boolean result = appointmentService.handlePaymentUpdate(paymentResult);

        verify(appointmentRepository).save(appointmentCaptor.capture());
        Appointment updatedAppointment = appointmentCaptor.getValue();
        assertNotNull(updatedAppointment);
        assertEquals(AppointmentStatus.CANCELLED, updatedAppointment.getStatus());
        assertTrue(result);
    }

    @Test
    void testHandlePaymentUpdateError() {
        String appointmentId = "123";
        PaymentResult paymentResult = createPaymentResult(appointmentId, true);

        when(appointmentRepository.findById(appointmentId)).thenThrow(new RuntimeException("Database error"));

        boolean result = appointmentService.handlePaymentUpdate(paymentResult);

        assertFalse(result);
    }

    private PaymentResult createPaymentResult(String appointmentId, boolean success) {
        PaymentResult paymentResult = new PaymentResult();
        paymentResult.setAppointmentId(appointmentId);
        paymentResult.setSuccess(success);
        return paymentResult;
    }

}
