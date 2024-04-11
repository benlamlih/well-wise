package net.benlamlih.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import net.benlamlih.appointmentservice.model.Appointment;
import net.benlamlih.appointmentservice.model.Availability;
import net.benlamlih.appointmentservice.repository.AppointmentRepository;
import net.benlamlih.appointmentservice.repository.AvailabilityRepository;
import net.benlamlih.appointmentservice.service.AppointmentService;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

	@Mock
	private AppointmentRepository appointmentRepository;

	@Mock
	private AvailabilityRepository availabilityRepository;

	@InjectMocks
	private AppointmentService appointmentService;

	@Test
	void testCreateAppointment() {
		String doctorId = "doctor123";
		String patientId = "patient123";
		LocalDate date = LocalDate.now();
		LocalTime startTime = LocalTime.of(10, 0);
		LocalTime endTime = LocalTime.of(11, 0);
		Availability availability = new Availability();
		availability.isAvailable(true);

		Mockito.when(availabilityRepository.findByDoctorIdAndDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
				Mockito.anyString(), Mockito.any(LocalDate.class), Mockito.any(LocalTime.class),
				Mockito.any(LocalTime.class))).thenReturn(Optional.of(availability));

		Mockito.when(appointmentRepository.existsByDoctorIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
				Mockito.anyString(), Mockito.any(LocalDate.class), Mockito.any(LocalTime.class),
				Mockito.any(LocalTime.class))).thenReturn(false);

		boolean result = appointmentService.createAppointment(doctorId, patientId, date, startTime, endTime);

		assertTrue(result);
		Mockito.verify(appointmentRepository).save(Mockito.any(Appointment.class));
	}

	@Test
	void testCancelAppointment() {
		String appointmentId = "appointment123";
		Appointment appointment = new Appointment();
		appointment.setId(appointmentId);

		Mockito.when(appointmentRepository.findById(Mockito.anyString())).thenReturn(Optional.of(appointment));

		boolean result = appointmentService.cancelAppointment(appointmentId);

		assertTrue(result);
		assertEquals("CANCELLED", appointment.getStatus());
		Mockito.verify(appointmentRepository).delete(Mockito.any(Appointment.class));
	}

	@Test
	void testConfirmAppointment() {
		String appointmentId = "appointment123";
		Appointment appointment = new Appointment();
		appointment.setId(appointmentId);

		Mockito.when(appointmentRepository.findById(Mockito.any())).thenReturn(Optional.of(appointment));

		boolean result = appointmentService.confirmAppointment(appointmentId);

		assertTrue(result);
		assertEquals("CONFIRMED", appointment.getStatus());
		Mockito.verify(appointmentRepository).save(Mockito.any(Appointment.class));

	}

}
