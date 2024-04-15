package net.benlamlih.appointmentservice.model;

import org.springframework.data.mongodb.core.mapping.Field;

public class PaymentDetails {

    @Field("receivedby")
    private String receivedBy;

    @Field("amountpaid")
    private Double amountPaid;

    @Field("note")
    private String note;

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
