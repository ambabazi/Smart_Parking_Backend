package com.smart.parking.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // returns events where now is between startTime and endTime
    Page<Event> findByStartTimeBeforeAndEndTimeAfterAndActiveTrue(
            LocalDateTime now1,
            LocalDateTime now2,
            Pageable pageable
    );

    List<Event> findByStartTimeBeforeAndEndTimeAfterAndActiveTrue(
            LocalDateTime now1,
            LocalDateTime now2
    );

    // returns expired events that haven't been deactivated yet
    List<Event> findByEndTimeBeforeAndActiveTrue(LocalDateTime now);

    Optional<Event> findByNameIgnoreCase(String name);
}