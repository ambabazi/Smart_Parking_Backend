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
}