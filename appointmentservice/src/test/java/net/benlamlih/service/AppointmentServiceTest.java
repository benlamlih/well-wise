package net.benlamlih.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
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
import net.benlamlih.appointmentservice.model.Payment;
import net.benlamlih.appointmentservice.model.PaymentMethod;
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

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        this.onlinePayment = new Payment();
        this.onlinePayment.setMethod(PaymentMethod.ONLINE);

        this.physicalPayment = new Payment();
        this.physicalPayment.setMethod(PaymentMethod.PHYSICAL);

        this.onlineRequest = createAppointmentRequest("doctor123", "patient123", today, now, onlinePayment);
        this.physicalRequest = createAppointmentRequest("doctor124", "patient124", today, now, physicalPayment);
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
    public void testBookAppointmentSuccess() {
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(new Appointment());
        boolean result = appointmentService.bookAppointment(this.onlineRequest);

        verify(appointmentRepository).save(any(Appointment.class));
        verify(userServiceClient).updateDoctorAvailability(eq("doctor123"), any(LocalDate.class), any(LocalTime.class),
                any(LocalTime.class), eq(false));
        verify(kafkaTemplate).send(eq("payment-request-topic"), any(Payment.class));
        assertTrue(result);
    }

    @Test
    public void testPhysicalPaymentBookAppointmentSuccess() {
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(new Appointment());
        boolean result = appointmentService.bookAppointment(this.physicalRequest);

        verify(appointmentRepository).save(any(Appointment.class));
        verify(userServiceClient).updateDoctorAvailability(eq("doctor124"), any(LocalDate.class), any(LocalTime.class),
                any(LocalTime.class), eq(false));
        verify(kafkaTemplate, never()).send(eq("payment-request-topic"), any(Payment.class));
        assertTrue(result);
    }

    @Test
    public void testBookAppointmentFailureOnSave() {
        doThrow(new RuntimeException("DB error")).when(appointmentRepository).save(any(Appointment.class));
        boolean result = appointmentService.bookAppointment(this.onlineRequest);
        verify(appointmentRepository).save(any(Appointment.class));
        assertFalse(result);
    }

    @Test
    public void testBookAppointmentFailure() {
        doThrow(new RuntimeException("Service unavailable")).when(userServiceClient)
                .updateDoctorAvailability(eq("doctor123"), any(LocalDate.class), any(LocalTime.class),
                        any(LocalTime.class), anyBoolean());

        boolean result = appointmentService.bookAppointment(this.onlineRequest);

        verify(appointmentRepository, never()).save(any(Appointment.class));
        assertFalse(result);
    }

    @Test
    void cancelAppointment() {
        String appointmentId = "123";
        String cancelledBy = "Patient";
        String reason = "Changed plans";
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setDoctorId("doctor123");

        Date startDateTime = DateTimeUtil.toDate(LocalTime.now(), LocalDate.now());
        Date endDateTime = DateTimeUtil.toDate(LocalTime.now().plusHours(1), LocalDate.now());
        appointment.setDateTime(startDateTime);
        appointment.setEndDateTime(endDateTime);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        boolean result = appointmentService.cancelAppointment(appointmentId, cancelledBy, reason);

        verify(appointmentRepository).save(appointmentCaptor.capture());
        assertEquals(AppointmentStatus.CANCELLED, appointmentCaptor.getValue().getStatus());
        verify(kafkaTemplate).send(eq("cancellation-topic"), cancellationMessageCaptor.capture());
        assertTrue(result);
    }

}
