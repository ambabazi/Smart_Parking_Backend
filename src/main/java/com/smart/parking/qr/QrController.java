package com.smart.parking.qr;

import com.smart.parking.reservation.Reservation;
import com.smart.parking.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verify-qr")
@RequiredArgsConstructor
public class QrController {

    private final ReservationRepository reservationRepository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('HOST', 'ADMIN')") // Only staff can verify
    public ResponseEntity<?> verifyQrToken(@RequestBody VerifyRequest request) {
        Reservation reservation = reservationRepository.findByQrCode(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid QR Token"));

        if (reservation.isVerified()) {
            return ResponseEntity.badRequest().body("Token already used!");
        }

        // Mark as verified and save
        reservation.setVerified(true);
        reservationRepository.save(reservation);

        return ResponseEntity.ok("APPROVED: Reservation verified for " + reservation.getSlotCount() + " slots.");
    }
}