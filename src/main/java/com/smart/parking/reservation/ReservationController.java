package com.smart.parking.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasAuthority('DRIVER')") // Only drivers can book spots
    public ResponseEntity<?> createReservation(
            @RequestBody BookingRequest request,
            Authentication authentication
    ) {
        try {
            // Spring Security automatically extracts the email from the JWT
            String userEmail = authentication.getName();

            Reservation reservation = reservationService.createReservation(request, userEmail);
            return ResponseEntity.ok(reservation);

        } catch (IllegalStateException e) {
            // Returns a 400 Bad Request if there aren't enough slots left
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred during booking.");
        }
    }
}