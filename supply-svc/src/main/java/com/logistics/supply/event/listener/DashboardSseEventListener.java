package com.logistics.supply.event.listener;

import com.logistics.supply.dto.DashboardData;
import com.logistics.supply.event.ApproveRequestItemEvent;
import com.logistics.supply.event.BulkRequestItemEvent;
import com.logistics.supply.event.FullPaymentEvent;
import com.logistics.supply.service.DashboardService;
import com.logistics.supply.service.DashboardSseBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for procurement lifecycle events and pushes fresh dashboard data
 * to all connected SSE subscribers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardSseEventListener {

    private final DashboardSseBroadcaster broadcaster;
    private final DashboardService dashboardService;

    @Async
    @EventListener
    public void onRequestApproved(ApproveRequestItemEvent event) {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onBulkRequest(BulkRequestItemEvent event) {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onPaymentCompleted(FullPaymentEvent event) {
        pushUpdate();
    }

    private void pushUpdate() {
        try {
            DashboardData data = dashboardService.getDashboardData();
            broadcaster.broadcast(data);
        } catch (Exception e) {
            log.warn("Failed to broadcast dashboard SSE update: {}", e.getMessage());
        }
    }
}
