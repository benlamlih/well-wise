package net.benlamlih.appointmentservice.client;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import net.benlamlih.appointmentservice.client.dto.AvailabilityUpdateRequest;

@Service
public class UserServiceClient {

    private final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
    private final RestTemplate restTemplate;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void updateDoctorAvailability(
            String doctorId, LocalDate date, LocalTime startTime, LocalTime endTime, Boolean available) {
        log.info("Updating doctor availability: doctorId={}, date={}, startTime={}, endTime={}, available={}",
                doctorId, date, startTime, endTime, available);
        String userServiceUrl = "http://userservice/availability/update";
        restTemplate.postForObject(
                userServiceUrl,
                new AvailabilityUpdateRequest(doctorId, date, startTime, endTime, available),
                Void.class);
    }
}
