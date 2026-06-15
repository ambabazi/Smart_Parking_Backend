package com.smart.parking.parking;

import com.smart.parking.common.ApiResponse;
import com.smart.parking.common.EntityIdentifierResolver;
import com.smart.parking.event.Event;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/parking-spaces")
@RequiredArgsConstructor
@Validated
public class ParkingController {

    private final ParkingService parkingService;
    private final EntityIdentifierResolver identifierResolver;

    // Public endpoints for searching and viewing parking spaces
    @GetMapping("/nearby")
    public ResponseEntity<Page<ParkingDTO>> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "2000") Double radius,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(parkingService.findNearby(lat, lng, radius, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(parkingService.getByIdOrUuidOrCode(id));
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<ParkingDTO> getByName(@PathVariable String name) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(parkingService.getByName(name));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ParkingDTO>> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(parkingService.searchByName(name, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<ParkingDTO>> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(parkingService.getAll(pageable));
    }

    @GetMapping("/event/{eventIdentifier}")
    public ResponseEntity<Page<ParkingDTO>> getByEvent(@PathVariable String eventIdentifier,
                                                       @PageableDefault(size = 10) Pageable pageable) {
        Event event = identifierResolver.resolveEvent(eventIdentifier);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic())
                .body(parkingService.getSpacesByEvent(event.getId(), pageable));
    }

    // Owner endpoints
    @PostMapping
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<ParkingSpaceDetailDTO>> registerParkingSpace(
            @Valid @RequestBody ParkingSpaceCreateDTO request,
            Authentication authentication) {
        ParkingSpace space = parkingService.createParkingSpace(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Parking space registered", toDetailDTO(space)));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<Page<ParkingSpaceDetailDTO>>> getMyParkingSpaces(
            Authentication authentication,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ParkingSpaceDetailDTO> dtos = parkingService.getMyParkingSpaces(authentication.getName(), pageable)
                .map(this::toDetailDTO);
        return ResponseEntity.ok(ApiResponse.success("Your parking spaces", dtos));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<ParkingSpaceDetailDTO>> updateParkingSpace(
            @PathVariable Long id,
            @Valid @RequestBody ParkingSpaceCreateDTO request,
            Authentication authentication) {
        ParkingSpace space = parkingService.updateParkingSpace(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Parking space updated", toDetailDTO(space)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ApiResponse<Void>> deleteParkingSpace(@PathVariable Long id, Authentication authentication) {
        parkingService.deleteParkingSpace(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Parking space deleted"));
    }

        @PatchMapping("/{identifier}/event-mode")
        @PreAuthorize("hasAuthority('HOST') or hasAuthority('ADMIN')")
        public ResponseEntity<ApiResponse<ParkingSpaceDetailDTO>> updateEventMode(
                        @PathVariable String identifier,
                        @Valid @RequestBody ParkingSpaceEventModeRequest request,
                        Authentication authentication) {
                ParkingSpace space = identifierResolver.resolveParkingSpace(identifier);
                boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
                if (!isAdmin && !space.getOwner().getEmail().equals(authentication.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("You can only update your own parking spaces"));
                }

                Event event = null;
                if (Boolean.TRUE.equals(request.getEventEnabled()) && request.getLinkedEventIdentifier() != null && !request.getLinkedEventIdentifier().isBlank()) {
                        event = identifierResolver.resolveEvent(request.getLinkedEventIdentifier());
                }

                ParkingSpace updated = parkingService.updateEventMode(space, request.getEventEnabled(), event);
                return ResponseEntity.ok(ApiResponse.success("Event mode updated", toDetailDTO(updated)));
        }

    private ParkingSpaceDetailDTO toDetailDTO(ParkingSpace space) {
        return ParkingSpaceDetailDTO.builder()
                .id(space.getId())
                .uuid(space.getUuid())
                .referenceCode(space.getReferenceCode())
                .name(space.getName())
                .address(space.getAddress())
                .latitude(space.getLatitude())
                .longitude(space.getLongitude())
                .totalSlots(space.getTotalSlots())
                .availableSlots(space.getAvailableSlots())
                .pricePerSlot(space.getPricePerSlot())
                .imageUrl(space.getImageUrl())
                .eventEnabled(space.getEventEnabled())
                .ownerId(space.getOwner().getId())
                .ownerName(space.getOwner().getFullName())
                .build();
    }
}
