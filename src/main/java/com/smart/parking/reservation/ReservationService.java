package com.smart.parking.reservation;

import com.smart.parking.auth.User;
import com.smart.parking.auth.UserRepository;
import com.smart.parking.common.EntityIdentifierResolver;
import com.smart.parking.notification.NotificationService;
import com.smart.parking.parking.ParkingSpace;
import com.smart.parking.parking.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final ParkingSpaceRepository spaceRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final EntityIdentifierResolver identifierResolver;

    @CacheEvict(cacheNames = {"reservationsByUser", "reservationsActive", "dashboardStats", "parkingSpaces", "parkingSpacesNearby", "parkingSpacesByEvent", "parkingSpacesByOwner"}, allEntries = true)
    @Transactional
    public Reservation createReservation(BookingRequest req, String userEmail) throws Exception {

        if (req.getEndTime().isBefore(req.getStartTime()) || req.getEndTime().isEqual(req.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ParkingSpace space = identifierResolver.resolveParkingSpace(req.getParkingSpaceId());

        int slotsRequested = req.getSlotCount();

        if (space.getAvailableSlots() < slotsRequested) {
            throw new IllegalStateException("Only " + space.getAvailableSlots() + " slots left");
        }

        space.setAvailableSlots(space.getAvailableSlots() - slotsRequested);
        spaceRepo.save(space);

        String qrToken = UUID.randomUUID().toString();
        BigDecimal totalAmount = calculateBookingTotal(space, slotsRequested, req.getStartTime(), req.getEndTime());

        Reservation res = Reservation.builder()
                .user(user)
                .parkingSpace(space)
                .slotCount(slotsRequested)
                .totalAmount(totalAmount)
                .qrCode(qrToken)
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .licensePlates(normalizeLicensePlates(req.getLicensePlates(), slotsRequested))
                .paid(false)
                .verified(false)
                .build();

        Reservation saved = reservationRepo.save(res);
        notificationService.sendBookingConfirmation(user.getPhone(), space.getName(), req.getStartTime().toString(), slotsRequested);
        return saved;
    }

    @CacheEvict(cacheNames = {"reservationsByUser", "reservationsActive", "dashboardStats", "parkingSpaces", "parkingSpacesNearby", "parkingSpacesByEvent", "parkingSpacesByOwner"}, allEntries = true)
    @Transactional
    public Reservation cancelReservation(String reservationIdentifier, String userEmail) throws Exception {
        Reservation res = identifierResolver.resolveReservation(reservationIdentifier);

        if (!res.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only cancel your own reservations");
        }

        ParkingSpace space = res.getParkingSpace();
        space.setAvailableSlots(space.getAvailableSlots() + res.getSlotCount());
        spaceRepo.save(space);

        res.setVerified(false);

        return reservationRepo.save(res);
    }

    @CacheEvict(cacheNames = {"reservationsByUser", "reservationsActive", "dashboardStats"}, allEntries = true)
    @Transactional
    public Reservation checkIn(String reservationIdentifier, String userEmail) throws Exception {
        Reservation res = identifierResolver.resolveReservation(reservationIdentifier);

        if (!res.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only check in to your own reservations");
        }

        if (!res.getStatus().equals("ACTIVE")) {
            throw new IllegalStateException("Cannot check in to a " + res.getStatus() + " reservation");
        }

        if (!res.isPaid()) {
            throw new IllegalStateException("Reservation must be paid before check-in");
        }

        res.setCheckedInAt(LocalDateTime.now());
        res.setStatus("CHECKED_IN");
        return reservationRepo.save(res);
    }

    @CacheEvict(cacheNames = {"reservationsByUser", "reservationsActive", "dashboardStats"}, allEntries = true)
    @Transactional
    public CheckoutResponse checkout(String reservationIdentifier, String userEmail) throws Exception {
        Reservation res = identifierResolver.resolveReservation(reservationIdentifier);

        if (!res.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only check out from your own reservations");
        }

        if (!res.getStatus().equals("CHECKED_IN")) {
            throw new IllegalStateException("Cannot check out; reservation status is " + res.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        res.setCheckedOutAt(now);
        res.setStatus("CHECKED_OUT");

        Duration overstay = Duration.between(res.getEndTime(), now);
        long overtimeMinutes = overstay.toMinutes();

        CheckoutResponse response = new CheckoutResponse();
        response.setReservationReferenceCode(res.getReferenceCode());
        response.setCheckedOutAt(now);
        response.setBookedUntil(res.getEndTime());

        if (overtimeMinutes > 0) {
            BigDecimal overtimeCharge = BigDecimal.valueOf(overtimeMinutes * 10L);
            res.setOvertimeAmount(overtimeCharge);
            response.setHasOvertime(true);
            response.setOvertimeMinutes(overtimeMinutes);
            response.setOvertimeAmount(overtimeCharge);
            response.setMessage("Overtime detected. Please pay " + overtimeCharge + " RWF before leaving.");
            reservationRepo.save(res);
            return response;
        }

        res.setStatus("COMPLETED");
        response.setHasOvertime(false);
        response.setMessage("Checked out successfully.");
        reservationRepo.save(res);
        return response;
    }

    @CacheEvict(cacheNames = {"reservationsByUser", "reservationsActive", "dashboardStats"}, allEntries = true)
    @Transactional
    public Reservation payOvertime(String reservationIdentifier, String userEmail, BigDecimal amount) throws Exception {
        Reservation res = identifierResolver.resolveReservation(reservationIdentifier);

        if (!res.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only pay for your own reservations");
        }

        if (res.getOvertimeAmount() == null || res.getOvertimeAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No overtime to pay for this reservation");
        }

        if (amount.compareTo(res.getOvertimeAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient payment. Owed: " + res.getOvertimeAmount());
        }

        res.setOvertimeAmount(BigDecimal.ZERO);
        res.setStatus("COMPLETED");
        return reservationRepo.save(res);
    }

    public Page<Reservation> getMyReservations(String userEmail, Pageable pageable) {
        return reservationRepo.findByUserEmail(userEmail, pageable);
    }

    public Page<Reservation> getActiveReservations(String ownerEmail, Pageable pageable) {
        return reservationRepo.findActiveByOwnerEmail(ownerEmail, pageable);
    }

    // Availability check used by frontend before creating reservation
    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(String parkingSpaceIdentifier, LocalDateTime startTime, LocalDateTime endTime, Integer requestedSlots) {
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }

        ParkingSpace space = identifierResolver.resolveParkingSpace(parkingSpaceIdentifier);
        Long parkingSpaceId = space.getId();

        Integer occupied = reservationRepo.countOccupiedSlots(parkingSpaceId, startTime, endTime);
        int available = space.getTotalSlots() - (occupied == null ? 0 : occupied);

        boolean isAvailable = available >= (requestedSlots == null ? 1 : requestedSlots);

        long hours = ChronoUnit.HOURS.between(startTime, endTime);
        if (hours == 0) hours = 1;

        double estimatedPrice = calculateBookingTotal(space, requestedSlots == null ? 1 : requestedSlots, startTime, endTime)
                .doubleValue();

        return new AvailabilityResponse(isAvailable, available, estimatedPrice, space.getPricePerSlot(), hours);
    }

    @Transactional(readOnly = true)
    public CurrentReservationDTO getCurrentParking(String userEmail) {
        var user = userRepo.findByEmail(userEmail).orElseThrow(() -> new IllegalArgumentException("User not found"));
        var opt = reservationRepo.findCurrentActiveReservation(user.getId());
        if (opt.isEmpty()) return null;
        Reservation r = opt.get();
        long minutesRemaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), r.getEndTime());
        return new CurrentReservationDTO(
                r.getReferenceCode(),
                r.getParkingSpace().getName(),
                r.getParkingSpace().getAddress(),
                r.getStartTime(),
                r.getEndTime(),
                r.getSlotCount(),
                r.getStatus(),
                minutesRemaining
        );
    }

    private String normalizeLicensePlates(java.util.List<String> plates, int slotCount) {
        if (plates == null || plates.isEmpty()) {
            return null;
        }
        java.util.List<String> cleaned = plates.stream()
                .map(p -> p == null ? "" : p.trim().toUpperCase())
                .filter(p -> !p.isBlank())
                .distinct()
                .toList();
        if (cleaned.isEmpty()) {
            return null;
        }
        if (cleaned.size() > slotCount) {
            throw new IllegalArgumentException(
                    "Provide at most " + slotCount + " license plate(s) for " + slotCount + " slot(s)");
        }
        return String.join(",", cleaned);
    }

    /**
     * Total = slots × pricePerSlot × duration (hours), minimum 15 minutes billed.
     * Matches the ParkShare frontend estimate so Flutterwave charges the same amount.
     */
    static BigDecimal calculateBookingTotal(ParkingSpace space, int slotCount,
                                            LocalDateTime start, LocalDateTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        if (minutes <= 0) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        double hours = Math.max(0.25, minutes / 60.0);
        double raw = space.getPricePerSlot() * slotCount * hours;
        return BigDecimal.valueOf(Math.round(raw));
    }
}