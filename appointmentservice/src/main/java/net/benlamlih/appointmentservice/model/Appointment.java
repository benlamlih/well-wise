package net.benlamlih.appointmentservice.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "appointments")
public class Appointment {

	@Id
	private String id;

	@Field("doctorId")
	private String doctorId;

	@Field("patientId")
	private String patientId;

	@Field("serviceType")
	private String serviceType;

	@Field("dateTime")
	private Date dateTime;

	@Field("endDateTime")
	private Date endDateTime;

	@Field("status")
	private AppointmentStatus status;

	@Field("details")
	private String details;

	@Field("payment")
	private Payment payment;

	private Appointment(Builder builder) {
		this.doctorId = builder.doctorId;
		this.patientId = builder.patientId;
		this.serviceType = builder.serviceType;
		this.dateTime = builder.dateTime;
		this.endDateTime = builder.endDateTime;
		this.status = builder.status;
		this.details = builder.details;
		this.payment = builder.payment;
	}

	public static class Builder {
		private String doctorId;
		private String patientId;
		private String serviceType;
		private Date dateTime;
		private Date endDateTime;
		private AppointmentStatus status;
		private String details;
		private Payment payment;

		public Builder doctorId(String doctorId) {
			this.doctorId = doctorId;
			return this;
		}

		public Builder patientId(String patientId) {
			this.patientId = patientId;
			return this;
		}

		public Builder serviceType(String serviceType) {
			this.serviceType = serviceType;
			return this;
		}

		public Builder dateTime(Date dateTime) {
			this.dateTime = dateTime;
			return this;
		}

		public Builder endDateTime(Date endDateTime) {
			this.endDateTime = endDateTime;
			return this;
		}

		public Builder status(AppointmentStatus status) {
			this.status = status;
			return this;
		}

		public Builder details(String details) {
			this.details = details;
			return this;
		}

		public Builder payment(Payment payment) {
			this.payment = payment;
			return this;
		}

		public Appointment build() {
			return new Appointment(this);
		}
	}

	public String getId() {
		return id;
	}

	public String getDoctorId() {
		return doctorId;
	}

	public String getPatientId() {
		return patientId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public Date getEndDateTime() {
		return endDateTime;
	}

	public AppointmentStatus getStatus() {
		return status;
	}

	public String getDetails() {
		return details;
	}

	public Payment getPayment() {
		return payment;
	}

}
