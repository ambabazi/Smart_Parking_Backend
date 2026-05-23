package com.smart.parking.event;

import com.smart.parking.common.EntityIdentifierResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class EventController {

    private final EventService eventService;
    private final EntityIdentifierResolver identifierResolver;

    // POST /events — admin only
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public EventResponse createEvent(
            @Valid @RequestBody EventRequest req) {
        return eventService.createEvent(req);
    }

    // GET /events/active — public
    @GetMapping("/active")
    public ResponseEntity<Page<EventResponse>> getActive(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
                .body(eventService.getActiveEvents(pageable));
    }

    // GET /events — admin only
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<EventResponse>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(eventService.getAllEvents(pageable));
    }

    // POST /events/{id}/parking-spaces — admin only
    @PostMapping("/{identifier}/parking-spaces")
    @PreAuthorize("hasAuthority('ADMIN')")
    public EventResponse linkParkingSpaces(
            @PathVariable String identifier,
            @Valid @RequestBody LinkParkingSpacesRequest req) {
        Event event = identifierResolver.resolveEvent(identifier);
        return eventService.linkParkingSpaces(event.getId(), req.getParkingSpaceIds());
    }

    // DELETE /events/{id}/deactivate — admin only
    @DeleteMapping("/{identifier}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deactivate(@PathVariable String identifier) {
        eventService.deactivateEvent(identifierResolver.resolveEvent(identifier).getId());
    }
}
