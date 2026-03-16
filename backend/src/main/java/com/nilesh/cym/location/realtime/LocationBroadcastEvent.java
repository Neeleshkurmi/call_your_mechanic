package com.nilesh.cym.location.realtime;

import java.time.Instant;

public record LocationBroadcastEvent(
        Long bookingId,
        String actorType,
        Long actorId,
        Double latitude,
        Double longitude,
        Instant recordedAt
) {
}
