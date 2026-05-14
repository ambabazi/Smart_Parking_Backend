package com.smart.parking.reservation;

import com.smart.parking.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> createReservation(
            @RequestBody BookingRequest request,
            Authentication authentication
    ) {
        try {
            String userEmail = authentication.getName();
            Reservation reservation = reservationService.createReservation(request, userEmail);
            return ResponseEntity.ok(toResponseDTO(reservation));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("We couldn’t create your reservation right now. Please try again."));
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<ApiResponse<List<ReservationResponseDTO>>> getMyReservations(
            Authentication authentication) {
        String userEmail = authentication.getName();
        List<Reservation> reservations = reservationRepository.findByUserEmail(userEmail);
        List<ReservationResponseDTO> dtos = reservations.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Your reservations", dtos));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .map(res -> ResponseEntity.ok(ApiResponse.success("Reservation found", toResponseDTO(res))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> cancelReservation(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Reservation reservation = reservationService.cancelReservation(id, userEmail);
            return ResponseEntity.ok(ApiResponse.success("Reservation cancelled", toResponseDTO(reservation)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("We couldn’t cancel the reservation right now. Please try again."));
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('HOST') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<ReservationResponseDTO>>> getActiveReservations(
            Authentication authentication) {
        String ownerEmail = authentication.getName();
        List<Reservation> reservations = reservationRepository.findActiveByOwnerEmail(ownerEmail);
        List<ReservationResponseDTO> dtos = reservations.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Active reservations", dtos));
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> checkIn(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Reservation reservation = reservationService.checkIn(id, userEmail);
            return ResponseEntity.ok(ApiResponse.success("Checked in successfully", toResponseDTO(reservation)));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("We couldn’t check you in right now. Please try again."));
        }
    }

    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> checkout(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            CheckoutResponse response = reservationService.checkout(id, userEmail);
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("We couldn’t complete checkout right now. Please try again."));
        }
    }

    @PostMapping("/{id}/pay-overtime")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> payOvertime(
            @PathVariable Long id,
            @RequestParam java.math.BigDecimal amount,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Reservation reservation = reservationService.payOvertime(id, userEmail, amount);
            return ResponseEntity.ok(ApiResponse.success("Overtime paid successfully", toResponseDTO(reservation)));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("We couldn’t process the overtime payment right now. Please try again."));
        }
    }

    private ReservationResponseDTO toResponseDTO(Reservation res) {
        return ReservationResponseDTO.builder()
                .id(res.getId())
                .userId(res.getUser().getId())
                .userFullName(res.getUser().getFullName())
                .userEmail(res.getUser().getEmail())
                .parkingSpaceId(res.getParkingSpace().getId())
                .parkingSpaceName(res.getParkingSpace().getName())
                .slotCount(res.getSlotCount())
                .startTime(res.getStartTime())
                .endTime(res.getEndTime())
                .totalAmount(res.getTotalAmount())
                .paid(res.getPaid())
                .verified(res.getVerified())
                .qrCode(res.getQrCode())
                .createdAt(res.getCreatedAt())
                .build();
    }
}