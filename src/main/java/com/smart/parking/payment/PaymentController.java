package com.smart.parking.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.parking.common.EntityIdentifierResolver;
import com.smart.parking.common.ResourceNotFoundException;
import com.smart.parking.reservation.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final EntityIdentifierResolver identifierResolver;
    private final ObjectMapper objectMapper;

    @Value("${app.flutterwave.secret.hash:}")
    private String flutterwaveSecretHash;

    @PostMapping("/initiate/{reservationIdentifier}")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> initiatePayment(@PathVariable String reservationIdentifier) {
        try {
            Reservation reservation = identifierResolver.resolveReservation(reservationIdentifier);

            if (Boolean.TRUE.equals(reservation.getPaid())) {
                return ResponseEntity.badRequest().body(Map.of("message", "This reservation has already been paid."));
            }

            String paymentLink = paymentService.initiatePayment(reservation);
            return ResponseEntity.ok(Map.of(
                    "paymentUrl", paymentLink,
                    "reservationReferenceCode", reservation.getReferenceCode()
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "verif-hash", required = false) String signature
    ) {
        if (signature == null || !signature.equals(flutterwaveSecretHash)) {
            return ResponseEntity.status(401).build();
        }

        try {
            FlutterwaveEvent event = objectMapper.readValue(rawBody, FlutterwaveEvent.class);
            paymentService.processWebhook(event);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Called by the frontend on the /payment/callback redirect to immediately
    // confirm the payment with Flutterwave (instead of waiting for a status poll
    // or a webhook that may never arrive).
    @PostMapping("/verify/{reservationIdentifier}")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> verifyPayment(
            @PathVariable String reservationIdentifier,
            Authentication authentication) {
        try {
            Reservation reservation = identifierResolver.resolveReservation(reservationIdentifier);
            if (!reservation.getUser().getEmail().equals(authentication.getName())) {
                return ResponseEntity.status(403).build();
            }
            var status = paymentService.getPaymentStatus(reservation.getId());
            boolean paid = status != null && "SUCCESS".equalsIgnoreCase(status.getStatus());
            return ResponseEntity.ok(Map.of(
                    "paid", paid,
                    "status", status != null ? status.getStatus() : "PENDING",
                    "reservationReferenceCode", reservation.getReferenceCode()
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status/{reservationIdentifier}")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> getPaymentStatus(
            @PathVariable String reservationIdentifier,
            Authentication authentication) {
        try {
            Reservation reservation = identifierResolver.resolveReservation(reservationIdentifier);
            String email = authentication.getName();
            if (!reservation.getUser().getEmail().equals(email)) {
                return ResponseEntity.status(403).build();
            }

            var status = paymentService.getPaymentStatus(reservation.getId());
            if (status == null) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(status);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
