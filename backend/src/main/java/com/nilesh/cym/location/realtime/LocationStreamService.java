package com.nilesh.cym.location.realtime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocationStreamService {

    private static final long EMITTER_TIMEOUT_MS = 0L;

    private final Map<Long, Set<SseEmitter>> bookingEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long bookingId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        bookingEmitters.computeIfAbsent(bookingId, ignored -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(bookingId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            removeEmitter(bookingId, emitter);
        });
        emitter.onError(ignored -> removeEmitter(bookingId, emitter));

        sendKeepAlive(emitter);
        return emitter;
    }

    public void broadcast(LocationBroadcastEvent event) {
        Set<SseEmitter> emitters = bookingEmitters.get(event.bookingId());
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("location-update")
                        .id(String.valueOf(System.currentTimeMillis()))
                        .data(event));
            } catch (IOException ex) {
                emitter.complete();
                removeEmitter(event.bookingId(), emitter);
            }
        }
    }


    @Scheduled(fixedDelay = 20000)
    public void broadcastKeepAlive() {
        for (Long bookingId : bookingEmitters.keySet()) {
            sendKeepAlive(bookingId);
        }
    }

    public void sendKeepAlive(Long bookingId) {
        Set<SseEmitter> emitters = bookingEmitters.get(bookingId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            sendKeepAlive(emitter);
        }
    }

    private void sendKeepAlive(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("keep-alive").comment("heartbeat"));
        } catch (IOException ex) {
            emitter.complete();
        }
    }

    private void removeEmitter(Long bookingId, SseEmitter emitter) {
        bookingEmitters.computeIfPresent(bookingId, (ignored, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}
