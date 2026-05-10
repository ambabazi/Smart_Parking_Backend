package com.smart.parking.reservation;

import com.smart.parking.auth.UserRepository;
import com.smart.parking.parking.ParkingSpace;
import com.smart.parking.parking.ParkingSpaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ParkingSpaceRepository spaceRepo;
    private final ReservationRepository  reservationRepo;
    private final UserRepository userRepo;
    //private final SimpMessagingTemplate   webSocket;   // injected after BE2-04

    // ─── @Transactional wraps the ENTIRE method in one DB transaction ───
    // If anything throws, ALL changes are rolled back automatically.
    // Without this, a crash after decrementing slots but before saving
    // the reservation would leave the DB in a broken state.
    @Transactional
    public ReservationResponse createReservation(BookingRequest req, Long userId) {

        // Step 1 — fetch the space WITH a pessimistic write lock.
        // This means: "lock this DB row so no other thread can read
        // or write it until this transaction finishes."
        // Two drivers booking the last slot at the same moment?
        // Thread B waits at this line until Thread A commits.
        ParkingSpace space = spaceRepo.findByIdWithLock(req.parkingSpaceId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Parking space not found"));

        // Step 2 — validate capacity
        if (space.getAvailableSlots() < req.slotCount()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only " + space.getAvailableSlots() + " slot(s) available");
        }

        // Step 3 — deduct slots atomically (inside the locked transaction)
        space.setAvailableSlots(space.getAvailableSlots() - req.slotCount());
        spaceRepo.save(space);

        // Step 4 — calculate total cost
        // hours = difference between end and start times
        // total = slotCount × pricePerSlot × hours
        long hours = ChronoUnit.HOURS.between(req.startTime(), req.endTime());
        double total = req.slotCount() * space.getPricePerSlot() * hours;

        // Step 5 — build and save the reservation
        Reservation res = Reservation.builder()
                .user(userRepo.getReferenceById(userId))
                .parkingSpace(space)
                .slotCount(req.slotCount())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .totalAmount(total)
                .qrCode(UUID.randomUUID().toString())   // unique token for QR
                .licensePlate(req.licensePlate())   // ← NEW: one line added
                .status(ReservationStatus.PENDING_PAYMENT)
                .paid(false)
                .verified(false)
                .build();
        reservationRepo.save(res);

        // Step 6 — broadcast new slot count to all connected clients
        // Every browser subscribed to /topic/slot-update receives this.
        // FE1 uses it to re-colour the map marker in real time.
        // (webSocket is null until BE2-04 — comment this out for now)
        //webSocket.convertAndSend("/topic/slot-update",
        //Map.of("parkingSpaceId", space.getId(),
        //"availableSlots",  space.getAvailableSlots(),
        //"licensePlate",  res.getLicensePlate()
        //));

        // Step 7 — return the response DTO
        return new ReservationResponse(
                res.getId(),
                space.getName(),
                res.getSlotCount(),
                res.getStartTime(),
                res.getEndTime(),
                res.getTotalAmount(),
                res.getStatus(),
                res.getQrCode(),
                res.getLicensePlate(),
                null   // paymentUrl — BE1 fills this in Sprint 3
        );
    }

    // GET /reservations/my — returns the logged-in user's bookings
    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepo
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(res -> new ReservationResponse(
                        res.getId(),
                        res.getParkingSpace().getName(),
                        res.getSlotCount(),
                        res.getStartTime(),
                        res.getEndTime(),
                        res.getTotalAmount(),
                        res.getStatus(),
                        res.getQrCode(),
                        res.getLicensePlate(),
                        null
                ))
                .toList();
    }
}
