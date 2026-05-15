package com.smart.parking.event;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventService eventService;

    @Scheduled(cron = "${scheduler.event-deactivation.cron}")
    public void deactivateExpiredEvents() {
        eventService.findExpiredEvents()
                .forEach(event -> {
                    eventService.deactivateEvent(event.getId());
                    System.out.println(
                            "[Scheduler] Deactivated expired event: "
                                    + event.getName()
                                    + " (id=" + event.getId() + ")");
                });
    }
}
