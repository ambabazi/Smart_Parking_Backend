package com.smart.parking.parking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {

    // ── Haversine formula ──────────────────────────────────
    // 6371000 = Earth's radius in metres
    // acos(cos(Δlat) * cos(Δlng) + sin(lat1)*sin(lat2))
    // gives the great-circle distance between two GPS points
    // We keep only rows where that distance ≤ :radiusMetres
    @Query("""
        SELECT p FROM ParkingSpace p
        WHERE (6371000 * acos(
            cos(radians(:lat)) * cos(radians(p.latitude))
            * cos(radians(p.longitude) - radians(:lng))
            + sin(radians(:lat)) * sin(radians(p.latitude))
        )) <= :radiusMetres
        """)
    List<ParkingSpace> findWithinRadius(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusMetres") Double radiusMetres
    );

    // Used by BE2-08: return only event-enabled spaces for one event
    List<ParkingSpace> findByCurrentEventId(Long eventId);

    // Used by BE2-06: bulk-activate spaces for an event
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ParkingSpace p WHERE p.id = :id")
    Optional<ParkingSpace> findByIdWithLock(@Param("id") Long id);

    // New: Find all parking spaces owned by a HOST
    @Query("SELECT p FROM ParkingSpace p WHERE p.owner.email = :ownerEmail")
    List<ParkingSpace> findByOwnerEmail(@Param("ownerEmail") String ownerEmail);

    // New: Find parking spaces by owner ID
    List<ParkingSpace> findByOwnerId(Long ownerId);
}
