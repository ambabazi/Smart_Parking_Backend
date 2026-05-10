package com.smart.parking.event;

import com.smart.parking.parking.ParkingSpace;
import com.smart.parking.parking.ParkingSpaceRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository  eventRepo;
    private final ParkingSpaceRepository spaceRepo;
    private final SimpMessagingTemplate webSocket;

    // ── POST /events ──────────────────────────────────────────────
    // @Transactional because we touch two tables:
    // 1. INSERT into events
    // 2. UPDATE parking_spaces (set event_enabled=true, current_event_id=X)
    // If step 2 fails, step 1 rolls back — no orphan events in the DB.
    @Transactional
    public EventResponse createEvent(EventRequest req) {

        // Step 1 — save the event first to get its generated ID
        Event event = Event.builder()
                .name(req.name())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .radiusMetres(req.radiusMetres())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .activatedSpaces(new ArrayList<>())
                .build();
        eventRepo.save(event);

        // Step 2 — Haversine query from BE2-01: find all spaces
        // within the event's radius of the venue coordinates
        List<ParkingSpace> nearby = spaceRepo
                .findWithinRadius(req.latitude(), req.longitude(), req.radiusMetres());

        // Step 3 — activate each nearby space
        // This is what "Event Mode" means: flip eventEnabled=true
        // and point currentEvent → this event
        nearby.forEach(space -> {
            space.setEventEnabled(true);
            space.setCurrentEvent(event);
        });
        spaceRepo.saveAll(nearby);

        // Step 4 — broadcast to all connected map viewers.
        // FE2 receives this and draws a green circle on the map instantly.
        webSocket.convertAndSend("/topic/events",
                (Object) Map.of(
                        "eventId",      event.getId(),
                        "eventName",    event.getName(),
                        "latitude",     event.getLatitude(),
                        "longitude",    event.getLongitude(),
                        "radiusMetres", event.getRadiusMetres()
                ));

        // Step 5 — return summary including how many spaces were activated
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getLatitude(),
                event.getLongitude(),
                event.getRadiusMetres(),
                event.getStartTime(),
                event.getEndTime(),
                nearby.size()          // e.g. 4 — admin sees "4 spaces activated"
        );
    }

    // ── Add this method to your existing EventService class ──────────
// This is the MANUAL deactivation path.
// BE2-07 calls the same logic on a timer every 5 minutes.
// Keeping the logic here means BE2-07 just calls this method —
// no duplication.

    @Transactional
    public void deactivateEvent(Long eventId) {

        // Step 1 — find the event or throw a clean 404
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        // Step 2 — find all spaces currently linked to this event
        List<ParkingSpace> spaces = spaceRepo.findByCurrentEventId(eventId);

        // Step 3 — reset each space back to normal mode
        // eventEnabled = false → marker returns to normal colour on map
        // currentEvent = null  → space is no longer linked to any event
        spaces.forEach(space -> {
            space.setEventEnabled(false);
            space.setCurrentEvent(null);
        });
        spaceRepo.saveAll(spaces);

        // Step 4 — broadcast deactivation so the map circle disappears instantly
        // FE2 receives this and removes the green circle overlay for this event
        webSocket.convertAndSend("/topic/events",
                (Object)Map.of(
                        "eventId",   eventId,
                        "active",    false,       // FE checks this flag
                        "eventName", event.getName()
                ));
    }

    // ── Also add this helper — used by BE2-07 scheduler ──────────────
// Returns all events whose endTime has already passed.
// BE2-07 loops over these and calls deactivateEvent() on each.
    public List<Event> findExpiredEvents() {
        return eventRepo.findByEndTimeBefore(LocalDateTime.now());
    }

    // ── GET /events/active ────────────────────────────────────────
    // Returns events currently in progress (startTime < now < endTime)
    public List<EventResponse> getActiveEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepo
                .findByStartTimeBeforeAndEndTimeAfter(now, now)
                .stream()
                .map(e -> new EventResponse(
                        e.getId(), e.getName(),
                        e.getLatitude(), e.getLongitude(),
                        e.getRadiusMetres(),
                        e.getStartTime(), e.getEndTime(),
                        e.getActivatedSpaces().size()
                ))
                .toList();
    }
}
