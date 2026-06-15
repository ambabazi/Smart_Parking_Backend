package com.smart.parking.payment;

import com.smart.parking.notification.NotificationService;
import com.smart.parking.reservation.Reservation;
import com.smart.parking.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    @Value("${app.flutterwave.secret.key:}")
    private String flwSecretKey;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private static final String FLW_BASE_URL = "https://api.flutterwave.com/v3";

    // 1. Generate Payment Link
    public String initiatePayment(Reservation res) {
        BigDecimal totalAmount = BigDecimal.valueOf(res.getParkingSpace().getPricePerSlot())
                .multiply(BigDecimal.valueOf(res.getSlotCount()));

        Map<String, Object> payload = Map.of(
                "tx_ref", "KP-" + res.getReferenceCode(),
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

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("data")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            return (String) data.get("link");
        }
        throw new RuntimeException("Failed to generate payment link");
    }

    // 2. Webhook callback (instant, but only if Flutterwave is configured to call us
    //    and the verif-hash matches). Delegates to the shared idempotent completion.
    @Transactional
    public void processWebhook(FlutterwaveEvent event) {
        if (event == null || event.getData() == null) return;
        if (!"successful".equalsIgnoreCase(event.getData().getStatus())) return;

        String txRef = event.getData().getTxRef();
        if (txRef == null) return;

        Reservation reservation = resolveReservation(stripPrefix(txRef))
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        BigDecimal amount = event.getData().getAmount() != null
                ? BigDecimal.valueOf(event.getData().getAmount())
                : reservation.getTotalAmount();

        if (!amountIsSufficient(reservation, amount)) {
            log.warn("Ignoring underpaid webhook for {} (paid {}, expected {})",
                    txRef, amount, reservation.getTotalAmount());
            return;
        }
        completePayment(reservation, String.valueOf(event.getData().getId()), amount);
    }

    // 3. Status check. Self-healing: if the payment is not recorded yet, verify
    //    directly with Flutterwave (covers a missed/unconfigured webhook). Returns
    //    SUCCESS, FAILED, or PENDING.
    @Transactional
    public PaymentStatusDTO getPaymentStatus(Long reservationId) {
        Optional<Payment> existing = paymentRepository.findByReservationId(reservationId);
        if (existing.isPresent() && "SUCCESS".equalsIgnoreCase(existing.get().getStatus())) {
            return toStatusDTO(existing.get());
        }

        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return existing.map(this::toStatusDTO).orElse(null);
        }

        // Ask Flutterwave for the live outcome when we have not yet recorded success.
        String liveStatus = "PENDING";
        if (!Boolean.TRUE.equals(reservation.getPaid())) {
            liveStatus = reconcileWithFlutterwave(reservation);
        }

        Optional<Payment> after = paymentRepository.findByReservationId(reservationId);
        if (after.isPresent() && "SUCCESS".equalsIgnoreCase(after.get().getStatus())) {
            return toStatusDTO(after.get());
        }
        return new PaymentStatusDTO(liveStatus, reservation.getTotalAmount(), null, reservation.getCreatedAt());
    }

    /**
     * Shared, idempotent completion used by both the webhook and the verify/reconcile
     * path. Marks the reservation paid, upserts the Payment as SUCCESS, and notifies.
     */
    @Transactional
    public Payment completePayment(Reservation reservation, String transactionId, BigDecimal paidAmount) {
        Optional<Payment> existing = paymentRepository.findByReservationId(reservation.getId());
        if (Boolean.TRUE.equals(reservation.getPaid())
                && existing.isPresent()
                && "SUCCESS".equalsIgnoreCase(existing.get().getStatus())) {
            return existing.get(); // already completed
        }

        reservation.setPaid(true);
        reservationRepository.save(reservation);

        Payment payment = existing.orElseGet(() -> Payment.builder().reservation(reservation).build());
        payment.setReservation(reservation);
        payment.setAmount(paidAmount != null ? paidAmount : reservation.getTotalAmount());
        payment.setStatus("SUCCESS");
        payment.setTransactionId(transactionId);
        Payment saved = paymentRepository.save(payment);

        notifyPaymentSuccess(reservation, saved.getAmount());
        return saved;
    }

    // --- helpers ---

    /**
     * Verifies the reservation's transaction directly with Flutterwave and returns the
     * live outcome as SUCCESS / FAILED / PENDING. On success it also completes the
     * payment (idempotently). Best-effort: any error maps to PENDING so the caller can
     * keep showing progress and retry on the next poll.
     */
    private String reconcileWithFlutterwave(Reservation reservation) {
        if (flwSecretKey == null || flwSecretKey.isBlank()) {
            return "PENDING"; // cannot verify without a secret key
        }
        try {
            String txRef = "KP-" + reservation.getReferenceCode();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + flwSecretKey);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    FLW_BASE_URL + "/transactions/verify_by_reference?tx_ref=" + txRef,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null || !(body.get("data") instanceof Map)) return "PENDING";
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) body.get("data");

            String status = String.valueOf(data.get("status"));
            BigDecimal amount = toBigDecimal(data.get("amount"));
            String transactionId = data.get("id") != null ? String.valueOf(data.get("id")) : null;

            if ("successful".equalsIgnoreCase(status) && amountIsSufficient(reservation, amount)) {
                completePayment(reservation, transactionId, amount);
                return "SUCCESS";
            }
            if ("failed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
                return "FAILED";
            }
            return "PENDING";
        } catch (Exception e) {
            // Verification is best-effort; the caller still returns the current (pending) status.
            log.debug("Flutterwave reconcile failed for reservation {}: {}",
                    reservation.getReferenceCode(), e.getMessage());
            return "PENDING";
        }
    }

    private Optional<Reservation> resolveReservation(String key) {
        return reservationRepository.findByReferenceCode(key)
                .or(() -> {
                    try {
                        return reservationRepository.findById(Long.parseLong(key));
                    } catch (NumberFormatException ex) {
                        return Optional.empty();
                    }
                });
    }

    private String stripPrefix(String txRef) {
        return txRef.startsWith("KP-") ? txRef.substring(3) : txRef;
    }

    private boolean amountIsSufficient(Reservation reservation, BigDecimal paidAmount) {
        BigDecimal expected = reservation.getTotalAmount();
        if (expected == null || paidAmount == null) return true; // cannot validate, accept
        return paidAmount.compareTo(expected) >= 0;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void notifyPaymentSuccess(Reservation reservation, BigDecimal amount) {
        try {
            String phone = reservation.getUser() != null ? reservation.getUser().getPhone() : null;
            if (phone == null || phone.isBlank()) return;
            String parking = reservation.getParkingSpace() != null
                    ? reservation.getParkingSpace().getName() : "your booking";
            String amountText = amount != null ? amount.stripTrailingZeros().toPlainString() : "?";
            notificationService.sendSms(phone, String.format(
                    "SmartPark: Payment of RWF %s received for %s (Ref %s). Your parking pass is now active.",
                    amountText, parking, reservation.getReferenceCode()));
        } catch (Exception e) {
            log.debug("Payment notification failed: {}", e.getMessage());
        }
    }

    private PaymentStatusDTO toStatusDTO(Payment p) {
        java.time.LocalDateTime processedAt = p.getReservation() != null ? p.getReservation().getCreatedAt() : null;
        return new PaymentStatusDTO(p.getStatus(), p.getAmount(), p.getTransactionId(), processedAt);
    }
}
