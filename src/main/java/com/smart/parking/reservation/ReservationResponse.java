package com.smart.parking.reservation;

import java.time.LocalDateTime;

// What we send back — matches Endpoints.md contract exactly
public record ReservationResponse(
        Long reservationId,
        String parkingSpaceName,
        Integer slotCount,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Double totalAmount,
        ReservationStatus status,
        String qrCode,
        String licensePlate,   // ← NEW: FE2 shows this on QR ticket + My Bookings
        String paymentUrl// null for now — BE1-07 fills this in Sprint 3

) {}
