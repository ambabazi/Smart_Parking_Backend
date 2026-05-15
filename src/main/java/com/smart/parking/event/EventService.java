package com.smart.parking.event;

import com.smart.parking.parking.ParkingSpaceRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.smart.parking.parking.ParkingSpace;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository        eventRepo;
    private final ParkingSpaceRepository spaceRepo;
    private final SimpMessagingTemplate webSocket;

    // ── POST /events ──────────────────────────────────────────────
    @Transactional
    public EventResponse createEvent(EventRequest req) {

        // 1 — save the event and capture the returned entity with its ID
        Event event = eventRepo.save(Event.builder()
                .name(req.name())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .radiusMetres(req.radiusMetres())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .activatedSpaces(new ArrayList<>())
                .build());

        // 2 — find all parking spaces within the event radius
        List<ParkingSpace> nearby = spaceRepo.findWithinRadius(
                req.latitude(), req.longitude(), req.radiusMetres());

        // 3 — activate each nearby space
        nearby.forEach(space -> {
            space.setEventEnabled(true);
            space.setCurrentEvent(event);
        });
        spaceRepo.saveAll(nearby);

        // 4 — broadcast to all connected map viewers
        webSocket.convertAndSend("/topic/events",
                new EventUpdateMessage(
                        event.getId(),
                        event.getName(),
                        event.getLatitude(),
                        event.getLongitude(),
                        event.getRadiusMetres(),
                        true
                ));

        return toResponse(event, nearby.size());
    }

    // ── GET /events/active ────────────────────────────────────────
    public List<EventResponse> getActiveEvents() {
        LocalDateTime now = LocalDateTime.now();
        return eventRepo
                .findByStartTimeBeforeAndEndTimeAfter(now, now)
                .stream()
                .map(e -> toResponse(e, e.getActivatedSpaces().size()))
                .toList();
    }

    // ── DELETE /events/{id}/deactivate ────────────────────────────
    @Transactional
    public void deactivateEvent(Long eventId) {

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        // reset all linked parking spaces
        List<ParkingSpace> spaces = spaceRepo.findByCurrentEventId(eventId);
        spaces.forEach(space -> {
            space.setEventEnabled(false);
            space.setCurrentEvent(null);
        });
        spaceRepo.saveAll(spaces);

        // mark event as no longer active
        event.setActive(false);
        eventRepo.save(event);

        // broadcast deactivation — FE removes the circle from the map
        webSocket.convertAndSend("/topic/events",
                new EventUpdateMessage(
                        eventId,
                        event.getName(),
                        event.getLatitude(),
                        event.getLongitude(),
                        event.getRadiusMetres(),
                        false
                ));
    }

    // ── used by scheduler ─────────────────────────────────────────
    public List<Event> findExpiredEvents() {
        return eventRepo.findByEndTimeBeforeAndActiveTrue(LocalDateTime.now());
    }

    // ── shared mapper ─────────────────────────────────────────────
    private EventResponse toResponse(Event e, int count) {
        return new EventResponse(
                e.getId(),
                e.getName(),
                e.getLatitude(),
                e.getLongitude(),
                e.getRadiusMetres(),
                e.getStartTime(),
                e.getEndTime(),
                count
        );
    }
}
