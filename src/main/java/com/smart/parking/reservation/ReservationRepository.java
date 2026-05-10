package com.smart.parking.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // GET /reservations/my — fetch all bookings for one user
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Used by BE1-08 Flutterwave webhook to find reservation by QR/txRef
    Optional<Reservation> findByQrCode(String qrCode);
}
