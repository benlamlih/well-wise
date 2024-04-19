package net.benlamlih.appointmentservice.model;

public class NotificationEvent {
    private String appointmentId;
    private String doctorId;
    private String patientId;
    private EventType eventType;
    private String message;

    private NotificationEvent(Builder builder) {
        this.appointmentId = builder.appointmentId;
        this.doctorId = builder.doctorId;
        this.patientId = builder.patientId;
        this.eventType = builder.eventType;
        this.message = builder.message;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getPatientId() {
        return patientId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getMessage() {
        return message;
    }

    public static class Builder {
        private String appointmentId;
        private String doctorId;
        private String patientId;
        private EventType eventType;
        private String message;

        public Builder withAppointmentId(String appointmentId) {
            this.appointmentId = appointmentId;
            return this;
        }

        public Builder withDoctorId(String doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public Builder withPatientId(String patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder withEventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public NotificationEvent build() {
            return new NotificationEvent(this);
        }
    }
}
