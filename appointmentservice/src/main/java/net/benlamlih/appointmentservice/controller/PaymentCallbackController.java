package net.benlamlih.appointmentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.benlamlih.appointmentservice.model.PaymentResult;
import net.benlamlih.appointmentservice.service.AppointmentService;

@RestController
@RequestMapping("/api/payment")
public class PaymentCallbackController {

    private final AppointmentService appointmentService;

    @Autowired
    public PaymentCallbackController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handlePaymentCallback(@RequestBody PaymentResult paymentResult) {
        boolean updateResult = appointmentService.handlePaymentUpdate(paymentResult);
        if (updateResult) {
            return ResponseEntity.ok("Payment processed successfully");
        } else {
            return ResponseEntity.status(500).body("Error processing payment update");
        }
    }
}
