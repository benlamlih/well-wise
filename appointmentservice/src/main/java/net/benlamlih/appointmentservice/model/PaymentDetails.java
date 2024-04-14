package net.benlamlih.appointmentservice.model;

import org.springframework.data.mongodb.core.mapping.Field;

public class PaymentDetails {

	@Field("receivedby")
	private String receivedBy;

	@Field("amountpaid")
	private Double amountPaid;

	@Field("note")
	private String note;
}
