package com.smart.parking.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;



import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // POST /events — admin only
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse createEvent(
            @Valid @RequestBody EventRequest req) {
        return eventService.createEvent(req);
    }

    // GET /events/active — public
    @GetMapping("/active")
    public List<EventResponse> getActive() {
        return eventService.getActiveEvents();
    }

    // DELETE /events/{id}/deactivate — admin only
    @DeleteMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable Long id) {
        eventService.deactivateEvent(id);
    }
}
