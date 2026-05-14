package com.smart.parking.parking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parking-spaces")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    // GET /parking-spaces/nearby?lat=-1.9441&lng=30.0619&radius=3000
    @GetMapping("/nearby")
    public ResponseEntity<List<ParkingDTO>> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "2000") Double radius
    ) {
        return ResponseEntity.ok(
                parkingService.findNearby(lat, lng, radius));
    }

    // GET /parking-spaces/1
    @GetMapping("/{id}")
    public ResponseEntity<ParkingDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(parkingService.getById(id));
    }

    // GET /parking-spaces
    @GetMapping
    public ResponseEntity<List<ParkingDTO>> getAll() {
        return ResponseEntity.ok(parkingService.getAll());
    }

    // GET /parking-spaces/event/1
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ParkingDTO>> getByEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                parkingService.getSpacesByEvent(eventId));
    }
}
