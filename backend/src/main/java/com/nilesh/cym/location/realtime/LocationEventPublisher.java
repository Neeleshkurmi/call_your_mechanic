package com.nilesh.cym.location.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LocationEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public LocationEventPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(LocationBroadcastEvent event) {
        String channel = channelForBooking(event.bookingId());
        try {
            redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException ignored) {
            // Keep location ingestion resilient; if publish fails, persistence still succeeds.
        }
    }

    public static String channelForBooking(Long bookingId) {
        return "booking:" + bookingId + ":location";
    }
}
