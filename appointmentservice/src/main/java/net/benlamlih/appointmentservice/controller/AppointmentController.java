package net.benlamlih.appointmentservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.benlamlih.appointmentservice.dto.AppointmentRequest;
import net.benlamlih.appointmentservice.dto.AppointmentResponse;
import net.benlamlih.appointmentservice.dto.CancellationRequest;
import net.benlamlih.appointmentservice.service.AppointmentService;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);
    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        logger.info("Fetching all appointments");
        List<AppointmentResponse> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @PostMapping
    public ResponseEntity<String> bookAppointment(@RequestBody AppointmentRequest request) {
        logger.info(
                "Attempting to book appointment for doctorId: {}, patientId: {}",
                request.getDoctorId(),
                request.getPatientId());

        boolean result = appointmentService.bookAppointment(request);
        if (result) {
            logger.info(
                    "Appointment booked successfully for doctorId: {}, patientId: {}",
                    request.getDoctorId(),
                    request.getPatientId());
            return ResponseEntity.ok("Appointment booked successfully");
        } else {
            logger.error(
                    "Failed to book appointment for doctorId: {}, patientId: {}",
                    request.getDoctorId(),
                    request.getPatientId());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to book appointment");
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelAppointment(@RequestBody CancellationRequest request) {
        logger.info("Attempting to cancel appointment with ID: {}", request.getAppointmentId());
        boolean result = appointmentService.cancelAppointment(
                request.getAppointmentId(), request.getCancelledBy(), request.getReason());

        if (result) {
            logger.info(
                    "Appointment cancelled successfully for ID: {}", request.getAppointmentId());
            return ResponseEntity.ok("Appointment cancelled successfully");
        } else {
            logger.error("Failed to cancel appointment with ID: {}", request.getAppointmentId());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to cancel appointment");
        }
    }
}
