package com.nilesh.cym.location.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisLocationSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final LocationStreamService locationStreamService;

    public RedisLocationSubscriber(
            ObjectMapper objectMapper,
            SimpMessagingTemplate messagingTemplate,
            LocationStreamService locationStreamService
    ) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.locationStreamService = locationStreamService;
    }

    public void handleMessage(String message) {
        try {
            LocationBroadcastEvent event = objectMapper.readValue(message, LocationBroadcastEvent.class);
            messagingTemplate.convertAndSend("/topic/bookings/" + event.bookingId() + "/location", event);
            locationStreamService.broadcast(event);
        } catch (JsonProcessingException ignored) {
            // Ignore malformed payloads.
        }
    }
}
