package net.benlamlih.appointmentservice.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import net.benlamlih.appointmentservice.dto.AppointmentResponse;
import net.benlamlih.appointmentservice.model.Appointment;

@Mapper
public interface AppointmentMapper {
	AppointmentMapper INSTANCE = Mappers.getMapper(AppointmentMapper.class);

	AppointmentResponse appointmentToAppointmentResponse(Appointment appointment);
}
