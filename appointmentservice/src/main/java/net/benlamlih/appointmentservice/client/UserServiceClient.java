package net.benlamlih.appointmentservice.client;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import net.benlamlih.appointmentservice.client.dto.AvailabilityUpdateRequest;

@Service
public class UserServiceClient {

	private final RestTemplate restTemplate;

	@Autowired
	public UserServiceClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void updateDoctorAvailability(String doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
		String userServiceUrl = "http://userservice/availability/update";
		restTemplate.postForObject(userServiceUrl, new AvailabilityUpdateRequest(doctorId, date, startTime, endTime),
				Void.class);
	}
}
