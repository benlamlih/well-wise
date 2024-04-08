package net.benlamlih.appointmentservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DoctorService {

    private final RestTemplate restTemplate;

    @Autowired
    public DoctorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<String> getDoctors() {
        String userServiceUrl = "http://userservice/users/doctors";
        ResponseEntity<List<String>> response = restTemplate.exchange(
            userServiceUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<String>>() {}
        );
        return response.getBody();
    }
}
