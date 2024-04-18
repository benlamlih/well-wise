package net.benlamlih.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import net.benlamlih.appointmentservice.model.Appointment;
import net.benlamlih.appointmentservice.model.EventType;
import net.benlamlih.appointmentservice.model.NotificationEvent;
import net.benlamlih.appointmentservice.repository.AppointmentRepository;
import net.benlamlih.appointmentservice.service.ReminderService;

class ReminderServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @InjectMocks
    private ReminderService reminderService;

    @Captor
    private ArgumentCaptor<NotificationEvent> notificationEventCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    void testSendReminders() {
        LocalDateTime now = LocalDateTime.of(2024, 4, 15, 10, 0);
        Appointment appointmentIn24Hours = createAppointment(now.plusHours(23), "1");
        Appointment appointmentIn1Hour = createAppointment(now.plusMinutes(59), "2");

        when(appointmentRepository.findAppointmentsBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(appointmentIn24Hours, appointmentIn1Hour));

       reminderService.sendReminders();

        verify(kafkaTemplate, times(2)).send(eq("reminder-topic"), notificationEventCaptor.capture());
        List<NotificationEvent> allEvents = notificationEventCaptor.getAllValues();
        assertEquals(2, allEvents.size());
        assertTrue(allEvents.stream().anyMatch(event -> event.getEventType() == EventType.REMINDER_24_HOUR));
        assertTrue(allEvents.stream().anyMatch(event -> event.getEventType() == EventType.REMINDER_1_HOUR));
    }

    private Appointment createAppointment(LocalDateTime dateTime, String id) {
        return new Appointment.Builder().dateTime(convertToDate(dateTime)).build();
    }

    private Date convertToDate(LocalDateTime dateTime) {
        return java.util.Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
