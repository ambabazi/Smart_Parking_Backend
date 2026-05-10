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

    // ── Add this endpoint to your existing ParkingSpaceController ────
// GET /parking-spaces/event/1
// @PathVariable extracts the {eventId} from the URL path

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ParkingDTO>> getByEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(parkingService.getSpacesByEvent(eventId));
    }
}
