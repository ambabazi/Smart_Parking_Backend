package com.smart.parking.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // returns events where now is between startTime and endTime
    List<Event> findByStartTimeBeforeAndEndTimeAfter(
            LocalDateTime now1,
            LocalDateTime now2
    );

    // returns expired events that haven't been deactivated yet
    List<Event> findByEndTimeBeforeAndActiveTrue(LocalDateTime now);
}