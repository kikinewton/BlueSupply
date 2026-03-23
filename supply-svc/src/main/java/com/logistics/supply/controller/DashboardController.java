package com.logistics.supply.controller;

import com.logistics.supply.dto.DashboardData;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.interfaces.projections.CancellationRateProjection;
import com.logistics.supply.interfaces.projections.CycleTimeProjection;
import com.logistics.supply.interfaces.projections.MonthlyTrendProjection;
import com.logistics.supply.model.PaymentAgingAnalysis;
import com.logistics.supply.model.RequestPerCurrentMonthPerDepartment;
import com.logistics.supply.model.SupplierPerformance;
import com.logistics.supply.repository.PaymentAgingAnalysisRepository;
import com.logistics.supply.repository.SupplierPerformanceRepository;
import com.logistics.supply.service.DashboardService;
import com.logistics.supply.service.DashboardSseBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;
  private final DashboardSseBroadcaster dashboardSseBroadcaster;
  private final SupplierPerformanceRepository supplierPerformanceRepository;
  private final PaymentAgingAnalysisRepository paymentAgingAnalysisRepository;

  @GetMapping("/data")
  public ResponseEntity<ResponseDto<DashboardData>> getDataForDashboard() {
    DashboardData data = dashboardService.getDashboardData();
    return ResponseDto.wrapSuccessResult(data, "FETCH DATA FOR DASHBOARD");
  }

  @GetMapping("/getAllRequestPerDepartmentForMonth")
  public ResponseEntity<ResponseDto<List<RequestPerCurrentMonthPerDepartment>>> requestPerDepartmentForMonth() {
    List<RequestPerCurrentMonthPerDepartment> allRequestPerDepartmentForMonth =
        dashboardService.getAllRequestPerDepartmentForMonth();
    return ResponseDto.wrapSuccessResult(allRequestPerDepartmentForMonth, "DATA REQUEST PER DEPARTMENT");
  }

  @GetMapping("/supplierPerformance")
  public ResponseEntity<ResponseDto<List<SupplierPerformance>>> getSupplierPerformance() {
    List<SupplierPerformance> results = supplierPerformanceRepository.findAll();
    return ResponseDto.wrapSuccessResult(results, "SUPPLIER PERFORMANCE DATA");
  }

  @GetMapping("/paymentAging")
  public ResponseEntity<ResponseDto<List<PaymentAgingAnalysis>>> getPaymentAging() {
    List<PaymentAgingAnalysis> results = paymentAgingAnalysisRepository.findAll();
    return ResponseDto.wrapSuccessResult(results, "PAYMENT AGING DATA");
  }

  @GetMapping("/cycleTime")
  public ResponseEntity<ResponseDto<List<CycleTimeProjection>>> getProcurementCycleTime() {
    List<CycleTimeProjection> results = dashboardService.getProcurementCycleTime();
    return ResponseDto.wrapSuccessResult(results, "PROCUREMENT CYCLE TIME BY DEPARTMENT");
  }

  @GetMapping("/cancellationRate")
  public ResponseEntity<ResponseDto<List<CancellationRateProjection>>> getCancellationRate() {
    List<CancellationRateProjection> results = dashboardService.getCancellationRate();
    return ResponseDto.wrapSuccessResult(results, "REQUEST CANCELLATION RATE BY DEPARTMENT");
  }

  @GetMapping("/trends")
  public ResponseEntity<ResponseDto<List<MonthlyTrendProjection>>> getMonthlyTrends(
      @RequestParam(defaultValue = "6") int months) {
    List<MonthlyTrendProjection> results = dashboardService.getMonthlyTrends(months);
    return ResponseDto.wrapSuccessResult(results, "MONTHLY TREND DATA");
  }

  /**
   * Server-Sent Events stream — pushes a fresh dashboard snapshot whenever a procurement
   * lifecycle event fires (request approval, bulk endorsement, full payment).
   * Requires a valid JWT in the Authorization header (same as all other /api/** endpoints).
   */
  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamDashboard() {
    return dashboardSseBroadcaster.subscribe();
  }
}
