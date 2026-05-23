package com.smart.parking.reservation;

import com.smart.parking.common.ApiResponse;
import com.smart.parking.common.DateTimeParseUtil;
import com.smart.parking.common.EntityIdentifierResolver;
import com.smart.parking.common.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Validated
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final EntityIdentifierResolver identifierResolver;

    @PostMapping
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<ApiResponse<ReservationResponseDTO>> createReservation(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication
    ) {
        try {
            String userEmail = authentication.getName();
            Reservation reservation = reservationService.createReservation(request, userEmail);
            return ResponseEntity.ok(ApiResponse.success("Reservation created", toResponseDTO(reservation)));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("We couldn’t create your reservation right now. Please try again."));
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<ApiResponse<Page<ReservationResponseDTO>>> getMyReservations(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        String userEmail = authentication.getName();
        Page<ReservationResponseDTO> dtos = reservationService.getMyReservations(userEmail, pageable)
                .map(this::toResponseDTO);
        return ResponseEntity.ok(ApiResponse.success("Your reservations", dtos));
    }

    @GetMapping("/check-availability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkAvailability(
            @RequestParam String parkingSpaceId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "1") Integer slotCount
    ) {
        try {
            java.time.LocalDateTime start = DateTimeParseUtil.parseFlexible(startTime);
            java.time.LocalDateTime end = DateTimeParseUtil.parseFlexible(endTime);
            AvailabilityResponse res = reservationService.checkAvailability(parkingSpaceId, start, end, slotCount);
            return ResponseEntity.ok(ApiResponse.success("Availability", res));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Could not check availability right now."));
        }
    }

    @GetMapping("/active/current")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> getCurrentParking(Authentication authentication) {
        String email = authentication.getName();
        CurrentReservationDTO dto = reservationService.getCurrentParking(email);
        if (dto == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(ApiResponse.success("Current reservation", dto));
    }

    @GetMapping("/{identifier}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getReservation(@PathVariable String identifier) {
        try {
            Reservation res = identifierResolver.resolveReservation(identifier);
            return ResponseEntity.ok(ApiResponse.success("Reservation found", toResponseDTO(res)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{identifier}/cancel")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> cancelReservation(
            @PathVariable String identifier,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Reservation reservation = reservationService.cancelReservation(identifier, userEmail);
            return ResponseEntity.ok(ApiResponse.success("Reservation cancelled", toResponseDTO(reservation)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("We couldn’t cancel the reservation right now. Please try again."));
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('HOST') or hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ReservationResponseDTO>>> getActiveReservations(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        String ownerEmail = authentication.getName();
        Page<ReservationResponseDTO> dtos = reservationService.getActiveReservations(ownerEmail, pageable)
                .map(this::toResponseDTO);
        return ResponseEntity.ok(ApiResponse.success("Active reservations", dtos));
    }

    @PostMapping("/{identifier}/check-in")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> checkIn(
            @PathVariable String identifier,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Reservation reservation = reservationService.checkIn(identifier, userEmail);
            return ResponseEntity.ok(ApiResponse.success("Checked in successfully", toResponseDTO(reservation)));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("We couldn’t check you in right now. Please try again."));
        }
    }

    @PostMapping("/{identifier}/checkout")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> checkout(
            @PathVariable String identifier,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            CheckoutResponse response = reservationService.checkout(identifier, userEmail);
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("We couldn’t complete checkout right now. Please try again."));
        }
    }

    @PostMapping("/{identifier}/pay-overtime")
    @PreAuthorize("hasAuthority('DRIVER')")
    public ResponseEntity<?> payOvertime(
            @PathVariable String identifier,
            @RequestParam @NotNull @Positive java.math.BigDecimal amount,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Reservation reservation = reservationService.payOvertime(identifier, userEmail, amount);
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
                .uuid(res.getUuid())
                .referenceCode(res.getReferenceCode())
                .userId(res.getUser().getId())
                .userFullName(res.getUser().getFullName())
                .userEmail(res.getUser().getEmail())
                .parkingSpaceReferenceCode(res.getParkingSpace().getReferenceCode())
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