package com.smart.parking.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByQrCode(String qrCode);

    // New: Find all reservations by user email
    @Query("SELECT r FROM Reservation r WHERE r.user.email = :email ORDER BY r.createdAt DESC")
    List<Reservation> findByUserEmail(@Param("email") String email);

    // New: Find all active reservations (current time within start/end) for a parking space owner
    @Query("""
        SELECT r FROM Reservation r 
        WHERE r.parkingSpace.owner.email = :ownerEmail 
        AND r.startTime <= CURRENT_TIMESTAMP 
        AND r.endTime >= CURRENT_TIMESTAMP
        AND r.paid = true
        """)
    List<Reservation> findActiveByOwnerEmail(@Param("ownerEmail") String ownerEmail);

    // New: Find all reservations for a parking space
    List<Reservation> findByParkingSpaceId(Long parkingSpaceId);

    // New: Find reservations for dashboard analytics
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.paid = true AND CAST(r.createdAt AS date) = CURRENT_DATE")
    Long countBookingsToday();

    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM Reservation r WHERE r.paid = true AND CAST(r.createdAt AS date) = CURRENT_DATE")
    java.math.BigDecimal revenueToday();

    @Query("""
        SELECT COUNT(r) FROM Reservation r 
        WHERE r.startTime <= CURRENT_TIMESTAMP 
        AND r.endTime >= CURRENT_TIMESTAMP 
        AND r.paid = true
        """)
    Long countActiveReservations();
}