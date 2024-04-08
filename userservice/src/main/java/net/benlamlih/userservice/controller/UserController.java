package net.benlamlih.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

	@GetMapping("/doctors")
	public List<String> listDoctors() {
		return Arrays.asList("Dr. Smith", "Dr. Jones", "Dr. Brown");
	}
}
