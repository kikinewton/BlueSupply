package com.logistics.supply.event.listener;

import com.logistics.supply.dto.DashboardData;
import com.logistics.supply.event.FloatEvent;
import com.logistics.supply.event.PettyCashEvent;
import com.logistics.supply.service.DashboardService;
import com.logistics.supply.service.DashboardSseBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for procurement lifecycle events and pushes fresh dashboard data
 * to all connected SSE subscribers.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DashboardSseEventListener {

    private final DashboardSseBroadcaster broadcaster;
    private final DashboardService dashboardService;

    @Async
    @EventListener
    public void onRequestApproved() {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onBulkRequest() {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onPaymentCompleted() {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onGrnCreated() {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onLpoAdded() {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onRequestCancelled() {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onPettyCashCreated(PettyCashEvent event) {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onPettyCashFundsReceived(FundsReceivedPettyCashListener.FundsReceivedPettyCashEvent event) {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onFloatCreatedOrEndorsed(FloatEvent event) {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onFloatFundsReceived(FundsReceivedFloatListener.FundsReceivedFloatEvent event) {
        pushUpdate();
    }

    @Async
    @EventListener
    public void onFloatRetirement(FloatRetirementListener.FloatRetirementEvent event) {
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
