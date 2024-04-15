package net.benlamlih.appointmentservice.client.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 
 */
public class AvailabilityUpdateRequest {

    private String doctorId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean available;

    public AvailabilityUpdateRequest(
            String doctorId, LocalDate date2, LocalTime startTime2, LocalTime endTime2, Boolean available) {
        this.doctorId = doctorId;
        this.date = date2;
        this.startTime = startTime2;
        this.endTime = endTime2;
        this.available = available;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

}
