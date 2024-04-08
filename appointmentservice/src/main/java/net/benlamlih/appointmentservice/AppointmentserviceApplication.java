package net.benlamlih.appointmentservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import net.benlamlih.appointmentservice.service.DoctorService;

@SpringBootApplication
public class AppointmentserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentserviceApplication.class, args);
	}

	@Autowired
	private DoctorService doctorService;

	@EventListener(ApplicationReadyEvent.class)
	public void testDoctorService() {
		List<String> doctors = doctorService.getDoctors();
		doctors.forEach(System.out::println);
	}
}
