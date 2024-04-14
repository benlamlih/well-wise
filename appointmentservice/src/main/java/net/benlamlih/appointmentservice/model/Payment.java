package net.benlamlih.appointmentservice.model;

import org.springframework.data.mongodb.core.mapping.Field;

public class Payment {

	@Field("method")
	private PaymentMethod method;

	@Field("status")
	private PaymentStatus status;

	@Field("paymentid")
	private String paymentId;

	@Field("details")
	private PaymentDetails details;
}

enum PaymentMethod {
	ONLINE, PHYSICAL;
}

enum PaymentStatus {
	PENDING, PAID, FAILED;
}
