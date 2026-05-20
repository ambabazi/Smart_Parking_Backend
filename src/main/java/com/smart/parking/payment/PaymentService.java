package com.smart.parking.payment;

import com.smart.parking.reservation.Reservation;
import com.smart.parking.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Value("${app.flutterwave.secret.key:}")
    private String flwSecretKey;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private static final String FLW_BASE_URL = "https://api.flutterwave.com/v3";

    // 1. Generate Payment Link [cite: 439, 440, 441, 455, 461]
    public String initiatePayment(Reservation res) {
                BigDecimal totalAmount = BigDecimal.valueOf(res.getParkingSpace().getPricePerSlot())
                .multiply(BigDecimal.valueOf(res.getSlotCount()));

        Map<String, Object> payload = Map.of(
                "tx_ref", "KP-" + res.getId(),
                "amount", totalAmount,
                "currency", "RWF",
                "redirect_url", frontendUrl + "/payment/callback",
                "customer", Map.of(
                        "email", res.getUser().getEmail(),
                        "name", res.getUser().getFullName()
                ),
                "customizations", Map.of(
                        "title", "Kigali Smart Parking",
                        "description", res.getSlotCount() + " slot(s) at " + res.getParkingSpace().getName()
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + flwSecretKey);
        headers.set("Content-Type", "application/json");

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                FLW_BASE_URL + "/payments",
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        // Extract the payment link from the Flutterwave response
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            return (String) data.get("link");
        }
        throw new RuntimeException("Failed to generate payment link");
    }

    // 2. Process Webhook Callback [cite: 466, 475, 476, 477, 478]
    public void processWebhook(FlutterwaveEvent event) {
        if ("successful".equals(event.getData().getStatus())) {
            String txRef = event.getData().getTxRef(); // Example: "KP-123"
            Long reservationId = Long.parseLong(txRef.replace("KP-", ""));

            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

            // Mark reservation as paid
            reservation.setPaid(true);
            reservationRepository.save(reservation);

            // Record the payment
            Payment payment = Payment.builder()
                    .reservation(reservation)
                    .amount(BigDecimal.valueOf(event.getData().getAmount()))
                    .status("SUCCESS")
                    .transactionId(String.valueOf(event.getData().getId()))
                    .build();
            paymentRepository.save(payment);
        }
    }
}