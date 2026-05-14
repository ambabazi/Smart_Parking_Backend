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

    // Haversine formula — finds spaces within radiusMetres of lat/lng
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

    // Used by event module — returns spaces linked to one event
    List<ParkingSpace> findByCurrentEventId(Long eventId);

    // Pessimistic lock — prevents double booking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ParkingSpace p WHERE p.id = :id")
    Optional<ParkingSpace> findByIdWithLock(@Param("id") Long id);
}
