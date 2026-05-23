package com.smart.parking.reservation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByQrCode(String qrCode);
    
    // Find by UUID or reference code
    Optional<Reservation> findByUuid(String uuid);
    Optional<Reservation> findByReferenceCode(String referenceCode);

    // New: Find all reservations by user email
    @Query("SELECT r FROM Reservation r WHERE r.user.email = :email ORDER BY r.createdAt DESC")
    Page<Reservation> findByUserEmail(@Param("email") String email, Pageable pageable);
    @Query("SELECT r FROM Reservation r WHERE r.user.email = :email ORDER BY r.createdAt DESC")
    List<Reservation> findByUserEmail(@Param("email") String email);

    // New: Find all active reservations (current time within start/end) for a parking space owner
    @Query("""
        SELECT r FROM Reservation r 
        WHERE r.parkingSpace.owner.email = :ownerEmail 
        AND r.startTime <= CURRENT_TIMESTAMP 
        AND r.endTime >= CURRENT_TIMESTAMP
        ORDER BY r.startTime DESC
        """)
    Page<Reservation> findActiveByOwnerEmail(@Param("ownerEmail") String ownerEmail, Pageable pageable);
    @Query("""
        SELECT r FROM Reservation r 
        WHERE r.parkingSpace.owner.email = :ownerEmail 
        AND r.startTime <= CURRENT_TIMESTAMP 
        AND r.endTime >= CURRENT_TIMESTAMP
        ORDER BY r.startTime DESC
        """)
    List<Reservation> findActiveByOwnerEmail(@Param("ownerEmail") String ownerEmail);

    // New: Find all reservations for a parking space
    Page<Reservation> findByParkingSpaceId(Long parkingSpaceId, Pageable pageable);
    List<Reservation> findByParkingSpaceId(Long parkingSpaceId);

    // New: Find reservations for dashboard analytics
    @Query(value = """
            SELECT COUNT(*) FROM reservations
            WHERE paid = true AND CAST(created_at AS DATE) = CURRENT_DATE
            """, nativeQuery = true)
    Long countBookingsToday();

    @Query(value = """
            SELECT COALESCE(SUM(total_amount), 0) FROM reservations
            WHERE paid = true AND CAST(created_at AS DATE) = CURRENT_DATE
            """, nativeQuery = true)
    java.math.BigDecimal revenueToday();

    @Query("""
        SELECT COUNT(r) FROM Reservation r 
        WHERE r.startTime <= CURRENT_TIMESTAMP 
        AND r.endTime >= CURRENT_TIMESTAMP 
        AND r.paid = true
        """)
    Long countActiveReservations();

    @Query("""
        SELECT COALESCE(SUM(r.slotCount), 0) FROM Reservation r
        WHERE r.parkingSpace.id = :parkingSpaceId
        AND r.status IN ('ACTIVE', 'CHECKED_IN')
        AND r.startTime < :endTime
        AND r.endTime > :startTime
    """)
    Integer countOccupiedSlots(@Param("parkingSpaceId") Long parkingSpaceId,
                               @Param("startTime") java.time.LocalDateTime startTime,
                               @Param("endTime") java.time.LocalDateTime endTime);

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.user.id = :driverId
        AND r.status = 'CHECKED_IN'
        AND r.startTime <= CURRENT_TIMESTAMP
        AND r.endTime > CURRENT_TIMESTAMP
        ORDER BY r.startTime DESC
    """)
    java.util.Optional<Reservation> findCurrentActiveReservation(@Param("driverId") Long driverId);

    // Dashboard methods for driver
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId AND r.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.user.id = :userId AND r.startTime > :time")
    Long countByUserIdAndStartTimeAfter(@Param("userId") Long userId, @Param("time") java.time.LocalDateTime time);

    // Dashboard methods for owner
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.parkingSpace.owner.id = :ownerId")
    Long countByOwnerId(@Param("ownerId") Long ownerId);

    @Query("""
        SELECT COUNT(r) FROM Reservation r 
        WHERE r.parkingSpace.owner.id = :ownerId 
        AND r.startTime <= CURRENT_TIMESTAMP 
        AND r.endTime >= CURRENT_TIMESTAMP
    """)
    Long countActiveReservationsByOwnerId(@Param("ownerId") Long ownerId);
}