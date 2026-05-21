package com.smart.parking.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByReservationId(Long reservationId);
    
    // Find by UUID or reference code
    Optional<Payment> findByUuid(String uuid);
    Optional<Payment> findByReferenceCode(String referenceCode);

    // Dashboard methods for driver
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.reservation.user.id = :userId AND p.status = 'SUCCESS'")
    BigDecimal sumPaymentsByUserId(@Param("userId") Long userId);

    // Dashboard methods for owner
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.reservation.parkingSpace.owner.id = :ownerId AND p.status = 'SUCCESS'")
    BigDecimal sumPaymentsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.reservation.parkingSpace.owner.id = :ownerId AND p.status = 'SUCCESS' AND CAST(p.createdAt AS date) = CURRENT_DATE")
    BigDecimal sumPaymentsByOwnerIdToday(@Param("ownerId") Long ownerId);
}