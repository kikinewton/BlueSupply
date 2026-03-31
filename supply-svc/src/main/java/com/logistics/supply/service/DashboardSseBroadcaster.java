package com.logistics.supply.service;

import com.logistics.supply.dto.DashboardData;
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
 * The initial data snapshot is fetched by the client via GET /api/dashboard/data on page load;
 * this broadcaster only pushes updates when procurement lifecycle events fire.
 */
@Slf4j
@Component
public class DashboardSseBroadcaster {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Registers a new SSE client. No initial data is pushed — the client fetches the
     * snapshot via the REST endpoint and listens here for live updates only.
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
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
