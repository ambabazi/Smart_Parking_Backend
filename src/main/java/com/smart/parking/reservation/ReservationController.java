package com.smart.parking.reservation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final BookingService bookingService;

    // POST /reservations
    // @Valid triggers the @NotNull @Min @Max checks on BookingRequest
    // @AuthenticationPrincipal extracts the logged-in user from the JWT
    // (BE1's JWT filter puts the UserDetails object into SecurityContext)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @Valid @RequestBody BookingRequest req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((AppUserDetails) userDetails).getId();
        return bookingService.createReservation(req, userId);
    }

    // GET /reservations/my
    // Returns all bookings for the currently authenticated user
    @GetMapping("/my")
    public List<ReservationResponse> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((AppUserDetails) userDetails).getId();
        return bookingService.getMyReservations(userId);
    }
}
