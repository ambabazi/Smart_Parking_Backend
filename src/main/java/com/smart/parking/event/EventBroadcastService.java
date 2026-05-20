package com.smart.parking.event;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    @Async("smartParkingTaskExecutor")
    public void publishEventUpdate(EventUpdateMessage message) {
        messagingTemplate.convertAndSend("/topic/events", message);
    }
}
