package com.smart.parking.qr;

import com.smart.parking.common.ApiResponse;
import com.smart.parking.common.EntityIdentifierResolver;
import com.smart.parking.auth.UserRepository;
import com.smart.parking.reservation.Reservation;
import com.smart.parking.reservation.ReservationRepository;
import com.smart.parking.reservation.QRVerificationResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@Validated
public class QrController {

    private final QrService qrService;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final EntityIdentifierResolver identifierResolver;

    /**
     * Generate a QR PNG for a reservation.
     * BE2 will call this after a booking is confirmed.
     * GET /api/qr/generate?reservationId=1&userId=2
     */
    @GetMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<byte[]> generateQr(
            @RequestParam @NotBlank String reservationId,
            org.springframework.security.core.Authentication authentication) {
        try {
            String email = authentication.getName();
            var user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
            Reservation reservation = identifierResolver.resolveReservation(reservationId);
            if (!reservation.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            byte[] png = qrService.generateQrBytes(reservation.getId(), user.getId());
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=qr-" + reservation.getReferenceCode() + ".png")
                .body(png);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verify a scanned QR code.
     * HOST scans the driver's QR at entry.
     * POST /api/qr/verify
     */
    @PostMapping("/verify")
    @PreAuthorize("hasAuthority('HOST') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<QRVerificationResponse>> verifyQr(
            @Valid @RequestBody VerifyRequest request,
            org.springframework.security.core.Authentication authentication) {
        try {
            String email = authentication.getName();
            var host = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));

            Reservation reservation = identifierResolver.resolveReservation(request.getReservationId());

            boolean valid = qrService.verifyQrContent(request.getQrContent(), reservation.getId());
            if (!valid) {
                return ResponseEntity.ok(ApiResponse.success("INVALID", new QRVerificationResponse("INVALID", false, null, null, null)));
            }

            // Verify host owns the parking space
            if (!reservation.getParkingSpace().getOwner().getId().equals(host.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("FORBIDDEN"));
            }

            if ("CHECKED_IN".equals(reservation.getStatus())) {
                return ResponseEntity.ok(ApiResponse.success("ALREADY_USED", new QRVerificationResponse("ALREADY_USED", false, reservation.getUser().getFullName(), null, null)));
            }

            if (java.time.LocalDateTime.now().isAfter(reservation.getEndTime())) {
                return ResponseEntity.ok(ApiResponse.success("EXPIRED", new QRVerificationResponse("EXPIRED", false, reservation.getUser().getFullName(), null, null)));
            }

            if (java.time.LocalDateTime.now().isBefore(reservation.getStartTime())) {
                return ResponseEntity.ok(ApiResponse.success("NOT_STARTED", new QRVerificationResponse("NOT_STARTED", false, reservation.getUser().getFullName(), null, null)));
            }

            // All good — mark checked in
            reservation.setStatus("CHECKED_IN");
            reservation.setCheckedInAt(java.time.LocalDateTime.now());
            reservationRepository.save(reservation);

            var detail = new com.smart.parking.reservation.ReservationDetail(
                    reservation.getId(), reservation.getStartTime(), reservation.getEndTime(), reservation.getParkingSpace().getName(), reservation.getTotalAmount()
            );

            return ResponseEntity.ok(ApiResponse.success("VALID", new QRVerificationResponse("VALID", true, reservation.getUser().getFullName(), reservation.getUser().getPhone(), detail)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("ERROR"));
        }
    }
}