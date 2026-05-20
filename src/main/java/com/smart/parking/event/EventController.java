package com.smart.parking.event;

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
    public ResponseEntity<Page<EventResponse>> getActive(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
                .body(eventService.getActiveEvents(pageable));
    }

    // DELETE /events/{id}/deactivate — admin only
    @DeleteMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable Long id) {
        eventService.deactivateEvent(id);
    }
}
