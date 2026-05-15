package com.smart.parking.reservation;

import com.smart.parking.auth.User;
import com.smart.parking.auth.UserRepository;
import com.smart.parking.parking.ParkingSpace;
import com.smart.parking.parking.ParkingSpaceRepository;
import com.smart.parking.qr.QrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final ParkingSpaceRepository spaceRepo;
    private final UserRepository userRepo;
    private final QrService qrService;

    @Transactional // Critical: Wraps the check + save atomically
    public Reservation createReservation(BookingRequest req, String userEmail) throws Exception {

        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Fetch parking space
        ParkingSpace space = spaceRepo.findById(req.getParkingSpaceId())
                .orElseThrow(() -> new IllegalArgumentException("Space not found"));

        int slotsRequested = req.getSlotCount(); // 1 to 5 [cite: 382]

        if (space.getAvailableSlots() < slotsRequested) {
            throw new IllegalStateException("Only " + space.getAvailableSlots() + " slots left");
        }

        // Deduct slots atomically [cite: 386]
        space.setAvailableSlots(space.getAvailableSlots() - slotsRequested);
        spaceRepo.save(space);

        // Generate the unique QR Token [cite: 109, 110, 111]
        String qrToken = UUID.randomUUID().toString();
        BigDecimal totalAmount = BigDecimal.valueOf(space.getPricePerSlot())
            .multiply(BigDecimal.valueOf(slotsRequested));

        // Build the reservation [cite: 389, 390, 391, 392, 393, 394, 395, 396]
        Reservation res = Reservation.builder()
                .user(user)
                .parkingSpace(space)
                .slotCount(slotsRequested)
            .totalAmount(totalAmount)
                .qrCode(qrToken)
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .paid(false) // Will be updated by Flutterwave later
                .verified(false)
                .build();

        return reservationRepo.save(res);
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId, String userEmail) throws Exception {
        Reservation res = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (!res.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only cancel your own reservations");
        }

        // Return slots to the parking space
        ParkingSpace space = res.getParkingSpace();
        space.setAvailableSlots(space.getAvailableSlots() + res.getSlotCount());
        spaceRepo.save(space);

        // Mark reservation as verified:false (cancelled state)
        res.setVerified(false);

        return reservationRepo.save(res);
    }

    @Transactional
    public Reservation checkIn(Long reservationId, String userEmail) throws Exception {
        Reservation res = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

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

    @Transactional
    public CheckoutResponse checkout(Long reservationId, String userEmail) throws Exception {
        Reservation res = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (!res.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only check out from your own reservations");
        }

        if (!res.getStatus().equals("CHECKED_IN")) {
            throw new IllegalStateException("Cannot check out; reservation status is " + res.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        res.setCheckedOutAt(now);
        res.setStatus("CHECKED_OUT");

        // Calculate overtime
        Duration overstay = Duration.between(res.getEndTime(), now);
        long overtimeMinutes = overstay.toMinutes();

        CheckoutResponse response = new CheckoutResponse();
        response.setReservationId(res.getId());
        response.setCheckedOutAt(now);
        response.setBookedUntil(res.getEndTime());

        if (overtimeMinutes > 0) {
            // Charge overtime: 10 RWF per minute
            BigDecimal overtimeCharge = BigDecimal.valueOf(overtimeMinutes * 10L);
            res.setOvertimeAmount(overtimeCharge);
            response.setHasOvertime(true);
            response.setOvertimeMinutes(overtimeMinutes);
            response.setOvertimeAmount(overtimeCharge);
            response.setMessage("Overtime detected. Please pay " + overtimeCharge + " RWF before leaving.");
            reservationRepo.save(res);
            return response;
        }

        // No overtime
        res.setStatus("COMPLETED");
        response.setHasOvertime(false);
        response.setMessage("Checked out successfully.");
        reservationRepo.save(res);
        return response;
    }

    @Transactional
    public Reservation payOvertime(Long reservationId, String userEmail, BigDecimal amount) throws Exception {
        Reservation res = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

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
}