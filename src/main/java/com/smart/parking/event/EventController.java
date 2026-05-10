package com.smart.parking.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // POST /events — admin only
    // @PreAuthorize checks the role from the JWT before the method runs.
    // A DRIVER hitting this endpoint gets 403 Forbidden automatically.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse createEvent(@Valid @RequestBody EventRequest req) {
        return eventService.createEvent(req);
    }

    // GET /events/active — public, no auth needed
    // FE2 polls or subscribes to this on map load
    @GetMapping("/active")
    public List<EventResponse> getActive() {
        return eventService.getActiveEvents();
    }

    // ── Add to EventController — lets admin manually kill an event ────
// Useful during demo if you want to show deactivation without
// waiting for the scheduler. Also good for testing BE2-07.

    @DeleteMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable Long id) {
        eventService.deactivateEvent(id);
    }
}
