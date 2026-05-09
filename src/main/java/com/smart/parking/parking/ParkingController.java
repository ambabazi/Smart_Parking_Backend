package com.smart.parking.parking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/parking-spaces")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    // GET /parking-spaces/nearby?lat=-1.9441&lng=30.0619&radius=2000
    // radius is in metres — default 2000m (2km) if not provided
    @GetMapping("/nearby")
    public ResponseEntity<List<ParkingDTO>> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "2000") Double radius
    ) {
        return ResponseEntity.ok(parkingService.findNearby(lat, lng, radius));
    }
}
