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

	public PaymentMethod getMethod() {
		return method;
	}

	public void setMethod(PaymentMethod method) {
		this.method = method;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public PaymentDetails getDetails() {
		return details;
	}

	public void setDetails(PaymentDetails details) {
		this.details = details;
	}

}

enum PaymentMethod {
	ONLINE, PHYSICAL;
}

enum PaymentStatus {
	PENDING, PAID, FAILED;
}
