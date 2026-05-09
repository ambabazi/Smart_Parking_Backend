package com.smartparking.repository;

import com.smartparking.model.ParkingSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    // Placeholder for Haversine query
    @Query("SELECT p FROM ParkingSpace p")
    List<ParkingSpace> findNearby(@Param("lat") double lat, @Param("lng") double lng, @Param("radius") double radius);
}
