package net.benlamlih.appointmentservice.service;

import static net.benlamlih.appointmentservice.util.DateTimeUtil.toLocalDateTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.benlamlih.appointmentservice.model.Appointment;
import net.benlamlih.appointmentservice.model.EventType;
import net.benlamlih.appointmentservice.model.NotificationEvent;
import net.benlamlih.appointmentservice.repository.AppointmentRepository;

@EnableScheduling
@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private final AppointmentRepository appointmentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReminderService(AppointmentRepository appointmentRepository,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.appointmentRepository = appointmentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Scheduled to run every hour
    @Scheduled(cron = "0 0 * * * *")
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Current time: {}", now);
        LocalDateTime oneDayAhead = now.plusDays(1);
        LocalDateTime oneHourAhead = now.plusHours(1);
        logger.info("Looking for appointments between now and one day ahead: {} to {}", now, oneDayAhead);

        List<Appointment> appointments = appointmentRepository.findAppointmentsBetween(now, oneDayAhead);
        appointments.forEach(appointment -> {
            LocalDateTime appointmentDateTime = toLocalDateTime(appointment.getDateTime());
            logger.info("Evaluating appointment: {}, scheduled for {}", appointment.getId(), appointmentDateTime);

            if (appointmentDateTime.isBefore(oneDayAhead) && appointmentDateTime.isAfter(now.plusMinutes(59))) {
                logger.info("Sending 24-hour reminder for appointment: {}", appointment.getId());
                sendReminder(appointment, EventType.REMINDER_24_HOUR);
            }
            if (appointmentDateTime.isBefore(oneHourAhead) && appointmentDateTime.isAfter(now)) {
                logger.info("Sending 1-hour reminder for appointment: {}", appointment.getId());
                sendReminder(appointment, EventType.REMINDER_1_HOUR);
            }
        });
    }

    private void sendReminder(Appointment appointment, EventType eventType) {
        NotificationEvent event = new NotificationEvent.Builder()
                .withAppointmentId(appointment.getId())
                .withEventType(eventType)
                .withMessage(String.format("Reminder: Your appointment is scheduled for %s.",
                        toLocalDateTime(appointment.getDateTime()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .build();
        kafkaTemplate.send("reminder-topic", event);
        logger.info("Sent {} for appointment: {}", eventType, appointment.getId());
    }
}
