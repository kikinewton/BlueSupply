package com.logistics.supply.mcp;

import com.logistics.supply.dto.DashboardData;
import com.logistics.supply.dto.PendingApprovalsDTO;
import com.logistics.supply.interfaces.projections.CancellationRateProjection;
import com.logistics.supply.interfaces.projections.CycleTimeProjection;
import com.logistics.supply.interfaces.projections.MonthlyTrendProjection;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.PaymentAgingAnalysisRepository;
import com.logistics.supply.repository.SupplierPerformanceRepository;
import com.logistics.supply.model.TrackRequestDto;
import com.logistics.supply.service.DashboardService;
import com.logistics.supply.service.LpoReportService;
import com.logistics.supply.service.TrackRequestStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Read-only MCP tools exposing BlueSupply procurement data to LLM clients.
 * All methods delegate to existing service-layer read operations — no mutations are exposed.
 *
 * <p>The Spring AI MCP server auto-configuration picks up {@code @Tool}-annotated methods
 * in {@code @Component} beans and registers them as MCP tool definitions.
 * The HTTP/SSE transport is served at the path configured by
 * {@code spring.ai.mcp.server.sse-message-endpoint} (default: {@code /mcp/message}).
 *
 * <p>All endpoints require a valid JWT Bearer token (same as {@code /api/**}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcurementMcpTools {

    private final DashboardService dashboardService;
    private final TrackRequestStatusService trackRequestStatusService;
    private final SupplierPerformanceRepository supplierPerformanceRepository;
    private final PaymentAgingAnalysisRepository paymentAgingAnalysisRepository;
    private final LpoReportService lpoReportService;

    @Tool(description =
            "Returns the current procurement dashboard snapshot: count of GRNs issued today, "
            + "payments made today, payments due within a week, total requests this month, "
            + "spend per department, and supplier spend analysis. "
            + "Use this to answer questions like: 'What is the current procurement status?', "
            + "'How many GRNs were issued today?', 'Which department has the highest spend this month?'")
    public DashboardData getDashboardMetrics() {
        return dashboardService.getDashboardData();
    }

    @Tool(description =
            "Tracks a single procurement request through all workflow stages by its request item ID. "
            + "Returns stage labels and timestamps for: endorsement, approval, LPO issuance, GRN issuance, "
            + "HOD GRN endorsement, procurement payment advice, and all payment approval stages "
            + "(account initiation, auditor check, FM authorisation, GM approval). "
            + "Use this to answer questions like: 'What stage is request 42 at?', "
            + "'Has the LPO been issued for request 15?'")
    public TrackRequestDto trackRequestStatus(
            @ToolParam(description = "The integer ID of the request item to track") int requestItemId) {
        return trackRequestStatusService.getRequestStage(requestItemId);
    }

    @Tool(description =
            "Returns per-supplier performance metrics: total LPOs issued, average delivery days "
            + "(time from LPO creation to GRN receipt), and payment completion rate as a percentage. "
            + "Use this to answer questions like: 'Which supplier has the best delivery time?', "
            + "'What is the payment completion rate for each supplier?'")
    public List<SupplierPerformance> getSupplierPerformance() {
        return supplierPerformanceRepository.findAll();
    }

    @Tool(description =
            "Returns all outstanding (PENDING or PARTIAL) payments grouped by aging bucket: "
            + "'0-30 days', '31-60 days', '61-90 days', '90+ days'. "
            + "Includes supplier name, payment amount, current status, and approval stage. "
            + "Use this to answer questions like: 'Which payments are overdue?', "
            + "'How much is outstanding for more than 60 days?'")
    public List<PaymentAgingAnalysis> getPaymentAging() {
        return paymentAgingAnalysisRepository.findAll();
    }

    @Tool(description =
            "Returns average procurement cycle time per department: average days from request creation "
            + "to endorsement and from creation to final approval. "
            + "Use this to answer questions like: 'Which department has the slowest endorsement process?', "
            + "'What is the average time from request to approval?'")
    public List<CycleTimeProjection> getProcurementCycleTime() {
        return dashboardService.getProcurementCycleTime();
    }

    @Tool(description =
            "Returns request cancellation and rejection counts per department along with total request counts. "
            + "Use this to answer questions like: 'Which department cancels the most requests?', "
            + "'What is the cancellation rate for the IT department?'")
    public List<CancellationRateProjection> getCancellationRate() {
        return dashboardService.getCancellationRate();
    }

    @Tool(description =
            "Returns monthly trend data for the past N months: request count and total procurement value per month. "
            + "Use this to answer questions like: 'How has procurement volume trended over the last 6 months?', "
            + "'Which month had the highest procurement spend this year?'")
    public List<MonthlyTrendProjection> getMonthlyTrends(
            @ToolParam(description = "Number of past months to include (e.g. 6, 12). Defaults to 6.")
            int months) {
        return dashboardService.getMonthlyTrends(months);
    }

    @Tool(description =
            "Returns a single-row snapshot of how many procurement items are stuck at each pipeline stage: "
            + "pending HOD endorsement, endorsed but not yet processed by procurement, "
            + "processed but awaiting HOD quotation review, LPO drafts awaiting HOD/GM approval, "
            + "and approved LPOs with no GRN yet issued. "
            + "Use this to answer questions like: 'Where is the procurement bottleneck right now?', "
            + "'How many LPOs are waiting for GRN?'")
    public ProcurementFunnel getLpoFunnel() {
        return lpoReportService.getFunnel().orElse(null);
    }

    @Tool(description =
            "Returns all GM-approved LPOs that have not yet had goods received (no GRN issued), "
            + "each with LPO reference, department, supplier name, and an aging bucket "
            + "('0-7 days', '8-14 days', '15-30 days', '30+ days') based on days since LPO creation. "
            + "Use this to answer questions like: 'Which LPOs are overdue for delivery?', "
            + "'What LPOs have been open for more than two weeks?'")
    public List<LpoAging> getLpoAging() {
        return lpoReportService.getLpoAging();
    }

    @Tool(description =
            "Returns total procurement spend and item count per request category on all processed purchase orders. "
            + "Use this to answer questions like: 'Which category has the highest spend?', "
            + "'How many items were procured under the IT equipment category?'")
    public List<SpendByCategory> getSpendByCategory() {
        return lpoReportService.getSpendByCategory();
    }

    @Tool(description =
            "Returns per-supplier award statistics: number of quotations submitted, number of LPOs awarded, "
            + "award rate as a percentage, and total LPO value. "
            + "Use this to answer questions like: 'Which supplier wins the most bids?', "
            + "'What is the total value of LPOs awarded to supplier X?', "
            + "'Which supplier has the lowest win rate?'")
    public List<SupplierAwardRate> getSupplierAwardRates() {
        return lpoReportService.getSupplierAwardRates();
    }

    @Tool(description =
            "Returns a count of items pending HOD endorsement and LPO drafts awaiting HOD/GM approval. "
            + "Use this to answer questions like: 'How many approvals are outstanding?', "
            + "'How many LPO drafts are waiting for sign-off?'")
    public PendingApprovalsDTO getPendingApprovals() {
        return lpoReportService.getPendingApprovals();
    }
}
