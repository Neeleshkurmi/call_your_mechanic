package com.nilesh.cym.location.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisLocationSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public RedisLocationSubscriber(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    public void handleMessage(String message) {
        try {
            LocationBroadcastEvent event = objectMapper.readValue(message, LocationBroadcastEvent.class);
            messagingTemplate.convertAndSend("/topic/bookings/" + event.bookingId() + "/location", event);
        } catch (JsonProcessingException ignored) {
            // Ignore malformed payloads.
        }
    }
}
