package com.logistics.supply.service;

import com.logistics.supply.dto.DashboardData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages active SSE connections for the real-time dashboard stream.
 * Clients subscribe via {@link #subscribe()} and receive pushes via {@link #broadcast(DashboardData)}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardSseBroadcaster {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final DashboardService dashboardService;

    /**
     * Creates a new SseEmitter, registers it, and immediately sends the current dashboard snapshot.
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("dashboard")
                    .data(dashboardService.getDashboardData()));
        } catch (IOException e) {
            log.warn("Failed to send initial dashboard snapshot to new SSE subscriber: {}", e.getMessage());
            emitters.remove(emitter);
        } catch (Exception e) {
            // getDashboardData() failed — keep emitter registered so it receives
            // the next event-driven broadcast rather than silently dropping the client.
            log.warn("Initial dashboard data unavailable for new SSE subscriber (will retry on next event): {}", e.getMessage());
        }
        return emitter;
    }

    /**
     * Sends a keepalive ping every 30 seconds to prevent proxies from dropping idle connections.
     */
    @Scheduled(fixedDelay = 30_000)
    public void heartbeat() {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("ping").data(""));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }

    /**
     * Pushes an updated dashboard snapshot to all active subscribers.
     */
    public void broadcast(DashboardData data) {
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("dashboard")
                        .data(data));
            } catch (IOException e) {
                log.debug("SSE subscriber disconnected, removing: {}", e.getMessage());
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }
}
