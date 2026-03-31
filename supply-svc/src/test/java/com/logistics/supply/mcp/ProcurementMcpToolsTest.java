package com.logistics.supply.mcp;

import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.DashboardData;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.fixture.RequestItemFixture;
import com.logistics.supply.interfaces.projections.CancellationRateProjection;
import com.logistics.supply.interfaces.projections.CycleTimeProjection;
import com.logistics.supply.interfaces.projections.MonthlyTrendProjection;
import com.logistics.supply.model.PaymentAgingAnalysis;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.SupplierPerformance;
import com.logistics.supply.model.TrackRequestDto;
import com.logistics.supply.repository.RequestItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ProcurementMcpToolsTest {

    @Autowired
    ProcurementMcpTools tools;

    @Autowired
    RequestItemRepository requestItemRepository;

    @Test
    void getDashboardMetrics_returnsNonNullSnapshot() {
        DashboardData result = tools.getDashboardMetrics();

        assertThat(result).isNotNull();
    }

    @Test
    void trackRequestStatus_returnsStageForSavedRequest() {
        RequestItem saved = requestItemRepository.save(
                RequestItemFixture.endorsed().approved().build());

        TrackRequestDto result = tools.trackRequestStatus(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getApproval()).isEqualTo(RequestApproval.APPROVED);
    }

    @Test
    void getSupplierPerformance_returnsNonNullList() {
        List<SupplierPerformance> result = tools.getSupplierPerformance();

        assertThat(result).isNotNull();
    }

    @Test
    void getPaymentAging_returnsNonNullList() {
        List<PaymentAgingAnalysis> result = tools.getPaymentAging();

        assertThat(result).isNotNull();
    }

    @Test
    void getProcurementCycleTime_returnsNonNullList() {
        List<CycleTimeProjection> result = tools.getProcurementCycleTime();

        assertThat(result).isNotNull();
    }

    @Test
    void getCancellationRate_returnsNonNullList() {
        List<CancellationRateProjection> result = tools.getCancellationRate();

        assertThat(result).isNotNull();
    }

    @Test
    void getMonthlyTrends_returnsNonNullListForSpecifiedMonths() {
        List<MonthlyTrendProjection> result = tools.getMonthlyTrends(6);

        assertThat(result).isNotNull();
    }
}
