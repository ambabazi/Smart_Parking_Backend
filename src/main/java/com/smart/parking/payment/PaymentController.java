package com.smart.parking.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.parking.reservation.Reservation;
import com.smart.parking.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationRepository reservationRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.flutterwave.secret.hash:}")
    private String flutterwaveSecretHash; // Used to verify Flutterwave webhooks

    @PostMapping("/initiate/{reservationId}")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> initiatePayment(@PathVariable Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.isPaid()) {
            return ResponseEntity.badRequest().body("Reservation is already paid.");
        }

        String paymentLink = paymentService.initiatePayment(reservation);
        return ResponseEntity.ok(Map.of("paymentUrl", paymentLink));
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "verif-hash", required = false) String signature
    ) {
        // 1. Verify the webhook is actually from Flutterwave (Security Check) [cite: 468, 471, 472]
        if (signature == null || !signature.equals(flutterwaveSecretHash)) {
            return ResponseEntity.status(401).build();
        }

        try {
            // 2. Parse and process the event [cite: 474]
            FlutterwaveEvent event = objectMapper.readValue(rawBody, FlutterwaveEvent.class);
            paymentService.processWebhook(event);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}