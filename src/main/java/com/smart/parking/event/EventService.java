package com.smart.parking.event;

import com.smart.parking.parking.ParkingSpace;
import com.smart.parking.parking.ParkingSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepo;
    private final ParkingSpaceRepository spaceRepo;
    private final EventBroadcastService broadcastService;

    // ── POST /events ──────────────────────────────────────────────
    @CacheEvict(cacheNames = {"activeEvents", "parkingSpacesByEvent", "parkingSpacesNearby", "parkingSpaces"}, allEntries = true)
    @Transactional
    public EventResponse createEvent(EventRequest req) {

        Event event = eventRepo.save(Event.builder()
                .name(req.name())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .radiusMetres(req.radiusMetres())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .activatedSpaces(new ArrayList<>())
                .build());

        List<ParkingSpace> nearby = spaceRepo.findWithinRadius(
                req.latitude(), req.longitude(), req.radiusMetres());

        nearby.forEach(space -> {
            space.setEventEnabled(true);
            space.setCurrentEvent(event);
        });
        spaceRepo.saveAll(nearby);

        broadcastService.publishEventUpdate(new EventUpdateMessage(
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
    @Cacheable(cacheNames = "activeEvents", key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<EventResponse> getActiveEvents(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return eventRepo
                .findByStartTimeBeforeAndEndTimeAfterAndActiveTrue(now, now, pageable)
                .map(e -> toResponse(e, e.getActivatedSpaces().size()));
    }

    @Cacheable(cacheNames = "allEvents", key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        return eventRepo.findAll(pageable).map(e -> toResponse(e, e.getActivatedSpaces().size()));
    }

    @CacheEvict(cacheNames = {"activeEvents", "allEvents", "parkingSpacesByEvent", "parkingSpacesNearby", "parkingSpaces"}, allEntries = true)
    @Transactional
    public EventResponse linkParkingSpaces(Long eventId, List<Long> parkingSpaceIds) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        List<ParkingSpace> spaces = spaceRepo.findAllById(parkingSpaceIds);
        if (spaces.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No parking spaces found for the supplied ids");
        }

        spaces.forEach(space -> {
            space.setEventEnabled(true);
            space.setCurrentEvent(event);
        });
        spaceRepo.saveAll(spaces);

        event.setActivatedSpaces(spaces);
        eventRepo.save(event);

        return toResponse(event, spaces.size());
    }

    // ── DELETE /events/{id}/deactivate ────────────────────────────
    @CacheEvict(cacheNames = {"activeEvents", "parkingSpacesByEvent", "parkingSpacesNearby", "parkingSpaces"}, allEntries = true)
    @Transactional
    public void deactivateEvent(Long eventId) {

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        List<ParkingSpace> spaces = spaceRepo.findByCurrentEventId(eventId);
        spaces.forEach(space -> {
            space.setEventEnabled(false);
            space.setCurrentEvent(null);
        });
        spaceRepo.saveAll(spaces);

        event.setActive(false);
        eventRepo.save(event);

        broadcastService.publishEventUpdate(new EventUpdateMessage(
                eventId,
                event.getName(),
                event.getLatitude(),
                event.getLongitude(),
                event.getRadiusMetres(),
                false
        ));
    }

    @CacheEvict(cacheNames = {"activeEvents", "allEvents", "parkingSpacesByEvent", "parkingSpacesNearby", "parkingSpaces"}, allEntries = true)
    @Transactional
    public ParkingSpace updateParkingSpaceEventMode(ParkingSpace space, Boolean eventEnabled, Event currentEvent) {
        space.setEventEnabled(Boolean.TRUE.equals(eventEnabled));
        space.setCurrentEvent(Boolean.TRUE.equals(eventEnabled) ? currentEvent : null);
        return spaceRepo.save(space);
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
