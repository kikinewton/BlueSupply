package com.logistics.supply.service;

import com.logistics.supply.dto.*;
import com.logistics.supply.interfaces.projections.CancellationRateProjection;
import com.logistics.supply.interfaces.projections.CycleTimeProjection;
import com.logistics.supply.interfaces.projections.MonthlyTrendProjection;
import com.logistics.supply.model.*;
import com.logistics.supply.repository.LocalPurchaseOrderDraftRepository;
import com.logistics.supply.repository.LpoAgingRepository;
import com.logistics.supply.repository.ProcurementFunnelRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.repository.SpendByCategoryRepository;
import com.logistics.supply.repository.SupplierAwardRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LpoReportService {

    private final ProcurementFunnelRepository procurementFunnelRepository;
    private final LpoAgingRepository lpoAgingRepository;
    private final SpendByCategoryRepository spendByCategoryRepository;
    private final SupplierAwardRateRepository supplierAwardRateRepository;
    private final RequestItemRepository requestItemRepository;
    private final LocalPurchaseOrderDraftRepository localPurchaseOrderDraftRepository;
    private final DashboardService dashboardService;

    public Optional<ProcurementFunnel> getFunnel() {
        return procurementFunnelRepository.findById(1);
    }

    public List<LpoAging> getLpoAging() {
        try {
            return lpoAgingRepository.findAll();
        } catch (Exception e) {
            log.error("Error fetching LPO aging: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<SpendByCategory> getSpendByCategory() {
        try {
            return spendByCategoryRepository.findAll();
        } catch (Exception e) {
            log.error("Error fetching spend by category: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<SupplierAwardRate> getSupplierAwardRates() {
        try {
            return supplierAwardRateRepository.findAll();
        } catch (Exception e) {
            log.error("Error fetching supplier award rates: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<CostOfGoodsPerDepartmentPerMonth> getSpendByDepartment() {
        return dashboardService.findCostPerDepartmentForCurrentMonth();
    }

    public List<SpendAnalysisDTO> getSupplierSpend() {
        return dashboardService.getSupplierSpendAnalysis();
    }

    public PendingApprovalsDto getPendingApprovals() {
        long pendingEndorsement = requestItemRepository.countPendingEndorsements();
        long lpoDraftsAwaiting = localPurchaseOrderDraftRepository.findDraftAwaitingApproval().size();
        return new PendingApprovalsDto(pendingEndorsement, lpoDraftsAwaiting);
    }

    public List<CycleTimeProjection> getCycleTime() {
        return dashboardService.getProcurementCycleTime();
    }

    public List<MonthlyTrendProjection> getMonthlyTrends(int months) {
        return dashboardService.getMonthlyTrends(months);
    }

    public List<CancellationRateProjection> getCancellationRate() {
        return dashboardService.getCancellationRate();
    }
}
