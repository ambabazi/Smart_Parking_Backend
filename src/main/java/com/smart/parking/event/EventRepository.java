package com.smart.parking.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // GET /events/active — Spring Data generates the SQL automatically.
    // "find events where startTime is before now AND endTime is after now"
    // i.e. the event is currently happening right now.
    List<Event> findByStartTimeBeforeAndEndTimeAfter(
            LocalDateTime now1,   // startTime < now
            LocalDateTime now2    // endTime   > now  (same value, passed twice)
    );

    // Used by BE2-07 auto-deactivation scheduler.
    // "find events that ended before this moment"
    List<Event> findByEndTimeBefore(LocalDateTime now);
}
