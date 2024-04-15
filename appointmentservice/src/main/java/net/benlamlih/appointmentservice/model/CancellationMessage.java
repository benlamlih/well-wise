package net.benlamlih.appointmentservice.model;

import java.time.Instant;

public class CancellationMessage {
    private String appointmentId;
    private String cancelledBy;
    private Instant timestamp;
    private String reason;

    public CancellationMessage(
            String appointmentId, String cancelledBy, Instant timestamp, String reason) {
        this.appointmentId = appointmentId;
        this.cancelledBy = cancelledBy;
        this.timestamp = timestamp;
        this.reason = reason;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
