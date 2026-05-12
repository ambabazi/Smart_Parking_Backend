package com.smart.parking.qr;

import com.smart.parking.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {

    private final QrService qrService;

    /**
     * Generate a QR PNG for a reservation.
     * BE2 will call this after a booking is confirmed.
     * GET /api/qr/generate?reservationId=1&userId=2
     */
    @GetMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> generateQr(
            @RequestParam Long reservationId,
            @RequestParam Long userId) {
        try {
            byte[] png = qrService.generateQrBytes(reservationId, userId);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=qr-" + reservationId + ".png")
                .body(png);
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
    public ResponseEntity<ApiResponse<Boolean>> verifyQr(
            @Valid @RequestBody VerifyRequest request) {
        boolean valid = qrService.verifyQrContent(
            request.getQrContent(), request.getReservationId());
        if (valid) {
            return ResponseEntity.ok(
                ApiResponse.success("QR is valid", true));
        }
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Invalid or expired QR code"));
    }
}