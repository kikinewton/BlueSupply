package com.logistics.supply.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private static final String SUMMARY_SYSTEM_PROMPT = """
      You are a procurement analyst writing a daily briefing for a supply chain manager. \
      Given the procurement metrics below, write a concise executive summary (3–5 sentences). \
      Highlight what stands out: urgency (payments due soon), volume, bottlenecks, or notable spend. \
      Be direct and professional. Do not repeat raw numbers verbatim — interpret them.""";

  private final DashboardService dashboardService;
  private final DashboardSseBroadcaster dashboardSseBroadcaster;
  private final SupplierPerformanceRepository supplierPerformanceRepository;
  private final PaymentAgingAnalysisRepository paymentAgingAnalysisRepository;
  private final ChatClient.Builder chatClientBuilder;
  private final ObjectMapper objectMapper;

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

  /**
   * Streams a natural-language executive summary of today's dashboard metrics.
   * Call this in parallel with /data — it returns SSE chunks as the LLM generates them.
   */
  @GetMapping(value = "/summary", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter getDashboardSummary() {
    DashboardData data = dashboardService.getDashboardData();

    String metricsPrompt;
    try {
      metricsPrompt = """
          Procurement metrics for today:
          - GRNs issued today: %d
          - Payments made today: %d
          - Payments due within 1 week: %d
          - Total requests this month: %d
          - Requests by category: %s
          - Spend by department this month: %s
          - Supplier spend breakdown: %s
          """.formatted(
          data.getCountOfGRNForToday(),
          data.getCountPaymentsMadeToday(),
          data.getCountOfPaymentDueWithinOneWeek(),
          data.getCountOfRequestPerCurrentMonth(),
          objectMapper.writeValueAsString(data.getRequestPerCategoryForToday()),
          objectMapper.writeValueAsString(data.getCostPerDepartmentForCurrentMonth()),
          objectMapper.writeValueAsString(data.getSupplierSpendAnalysis())
      );
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize dashboard data for summary prompt", e);
      metricsPrompt = "GRNs today: %d, Payments today: %d, Payments due soon: %d, Requests this month: %d"
          .formatted(data.getCountOfGRNForToday(), data.getCountPaymentsMadeToday(),
              data.getCountOfPaymentDueWithinOneWeek(), data.getCountOfRequestPerCurrentMonth());
    }

    SseEmitter emitter = new SseEmitter(120_000L);
    ChatClient chatClient = chatClientBuilder.build();

    chatClient.prompt()
        .system(SUMMARY_SYSTEM_PROMPT)
        .user(metricsPrompt)
        .stream()
        .content()
        .timeout(Duration.ofSeconds(90))
        .doOnError(Exception.class, e -> {
          log.warn("Dashboard summary stream error ({}): {}", e.getClass().getSimpleName(), e.getMessage());
          try {
            emitter.send(SseEmitter.event()
                .name("error")
                .data(objectMapper.writeValueAsString(Map.of("error", "LLM service unavailable"))));
          } catch (IOException ignored) {}
        })
        .onErrorResume(Exception.class, e -> Flux.empty())
        .subscribe(
            chunk -> {
              try {
                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of("content", chunk))));
              } catch (IOException e) {
                emitter.completeWithError(e);
              }
            },
            emitter::completeWithError,
            () -> {
              try {
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
              } catch (IOException e) {
                emitter.completeWithError(e);
              }
            }
        );

    return emitter;
  }
}
