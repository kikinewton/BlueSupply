package com.logistics.supply.controller;

import com.logistics.supply.dto.PagedResponseDto;
import com.logistics.supply.dto.PendingApprovalsDto;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@RestController
@Slf4j
@CrossOrigin(
    origins = {
      "https://etornamtechnologies.github.io/skyblue-request-frontend-react",
      "http://localhost:4000"
    },
    allowedHeaders = "*")
@RequiredArgsConstructor
public class ReportController {

  private final ExcelService excelService;
  private final GrnReportService grnReportService;
  private final PaymentReportService paymentReportService;
  private final ProcuredItemReportService procuredItemReportService;
  private final FloatAgeingAnalysisService floatAgeingAnalysisService;
  private final PettyCashPaymentReportService pettyCashPaymentReportService;
  private final FloatOrderPaymentReportService floatOrderPaymentReportService;
  private final LpoReportService lpoReportService;

  @GetMapping("/res/procurement/procuredItemsReport")
  public ResponseEntity<?> getFile(
      @RequestParam(required = false, defaultValue = "false") boolean download,
      @RequestParam(required = false) String supplier,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize)
      throws GeneralException {

    if (periodStart == null || periodEnd == null) {
      return Helper.failedResponse("FAILED TO GENERATE REPORT");
    }
    Date start = toDate(periodStart);
    Date end   = toDate(periodEnd);

    if (supplier != null) {
      Page<ProcuredItemReport> procured =
          procuredItemReportService.findBySupplier(pageNo, pageSize, start, end, supplier);
      return PagedResponseDto.wrapSuccessResult(procured, Constants.FETCH_SUCCESSFUL);
    }

    if (download) {
      InputStreamResource file =
          new InputStreamResource(excelService.createProcuredItemsDataSheet(start, end));
      String filename = "items_report_" + UUID.randomUUID().toString().substring(7) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
          .body(file);
    }

    Page<ProcuredItemReport> procured =
        procuredItemReportService.findAllBetween(pageNo, pageSize, start, end);
    return PagedResponseDto.wrapSuccessResult(procured, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping("/res/accounts/paymentReport")
  public ResponseEntity<?> getPaymentReportFile(
      @RequestParam(required = false, defaultValue = "false") boolean download,
      @RequestParam(required = false) String supplier,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd)
      throws GeneralException {

    if (periodStart == null || periodEnd == null) {
      return Helper.failedResponse("FAILED TO GENERATE REPORT");
    }
    Date start = toDate(periodStart);
    Date end   = toDate(periodEnd);

    if (download) {
      InputStreamResource file =
          new InputStreamResource(excelService.createPaymentDataSheet(start, end));
      String filename = "payments_report_" + UUID.randomUUID().toString().substring(7) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(file);
    }

    Page<PaymentReport> reports = supplier != null
        ? paymentReportService.findBySupplier(pageNo, pageSize, start, end, supplier)
        : paymentReportService.findBetweenDate(pageNo, pageSize, start, end);

    return PagedResponseDto.wrapSuccessResult(reports, Constants.FETCH_SUCCESSFUL);
  }

  private static Date toDate(LocalDate ld) {
    return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }

  @GetMapping("/res/accounts/pettyCashPaymentReport")
  public ResponseEntity<?> getPettyCashPaymentReportFile(
      @RequestParam(required = false, defaultValue = "false") boolean download,
      @RequestParam(required = false) String requesterEmail,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize)
      throws GeneralException {

    if (periodStart == null || periodEnd == null) {
      return Helper.failedResponse("FAILED TO GENERATE REPORT");
    }
    Date start = toDate(periodStart);
    Date end   = toDate(periodEnd);

    if (download) {
      InputStreamResource file =
          new InputStreamResource(excelService.createPettyCashPaymentDataSheet(start, end));
      String filename = "petty_cash_payments_report_" + UUID.randomUUID().toString().substring(7) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(file);
    }

    Page<PettyCashPaymentReport> ptc = requesterEmail != null
        ? pettyCashPaymentReportService.findByRequestedByEmail(pageNo, pageSize, start, end, requesterEmail)
        : pettyCashPaymentReportService.findBetweenDate(pageNo, pageSize, start, end);

    return PagedResponseDto.wrapSuccessResult(ptc, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping("/res/accounts/floatAgeingAnalysisReport")
  public ResponseEntity<?> getFloatAgeingAnalysisReportFile(
      @RequestParam(required = false, defaultValue = "false") boolean download,
      @RequestParam(required = false) String requesterEmail,
      @RequestParam(required = false) String staffId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize) {
    try {

      if (download && periodStart != null && periodEnd != null) {
        InputStreamResource file =
            new InputStreamResource(
                excelService.createFloatAgingAnalysis(toDate(periodStart), toDate(periodEnd)));
        String filename = "float_ageing_analysis" + UUID.randomUUID().toString().substring(7) + ".xlsx";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
      }

      if (requesterEmail != null && periodStart != null && periodEnd != null) {
        try {
          Page<FloatAgingAnalysis> result =
              floatAgeingAnalysisService.findFloatAnalysisByRequesterEmail(pageNo, pageSize, requesterEmail);
          return PagedResponseDto.wrapSuccessResult(result, Constants.FETCH_SUCCESSFUL);
        } catch (Exception e) {
          log.error("Failed to fetch float ageing analysis by requester email: {}", requesterEmail, e);
        }
      }

      if (staffId != null && periodStart != null && periodEnd != null) {
        try {
          Page<FloatAgingAnalysis> result =
              floatAgeingAnalysisService.findFloatAnalysisByStaffId(pageNo, pageSize, staffId);
          return PagedResponseDto.wrapSuccessResult(result, Constants.FETCH_SUCCESSFUL);
        } catch (Exception e) {
          log.error("Failed to fetch float ageing analysis by staff id: {}", staffId, e);
        }
      }

      if (periodStart != null && periodEnd != null) {
        Page<FloatAgingAnalysis> res =
            floatAgeingAnalysisService.findBetweenDate(pageNo, pageSize, toDate(periodStart), toDate(periodEnd));
        return PagedResponseDto.wrapSuccessResult(res, Constants.FETCH_SUCCESSFUL);
      }

      if (!download) {
        Page<FloatAgingAnalysis> result = floatAgeingAnalysisService.findAllFloatAnalysis(pageNo, pageSize);
        return PagedResponseDto.wrapSuccessResult(result, Constants.FETCH_SUCCESSFUL);
      }

    } catch (Exception e) {
      log.error("Failed to generate float ageing analysis report", e);
    }
    return Helper.failedResponse("FAILED TO GENERATE REPORT");
  }

  @GetMapping("/res/accounts/floatOrderPaymentReport")
  public ResponseEntity<?> getFloatOrderPaymentReport(
      @RequestParam(required = false, defaultValue = "false") boolean download,
      @RequestParam(required = false) String staffId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize) throws GeneralException {

    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("fundsAllocatedDate").descending());

    if (download && periodStart != null && periodEnd != null) {
      InputStreamResource file =
          new InputStreamResource(
              excelService.createFloatOrderPaymentDataSheet(toDate(periodStart), toDate(periodEnd)));
      String filename = "float_order_payment_report_" + UUID.randomUUID().toString().substring(7) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(file);
    }

    if (staffId != null && periodStart != null && periodEnd != null) {
      Page<FloatOrderPaymentReport> byRequesterStaffId =
          floatOrderPaymentReportService.findByRequesterStaffId(staffId, pageable);
      return PagedResponseDto.wrapSuccessResult(byRequesterStaffId, Constants.FETCH_SUCCESSFUL);
    }

    if (periodStart != null && periodEnd != null) {
      Page<FloatOrderPaymentReport> betweenFundsPaidDate =
          floatOrderPaymentReportService.findBetweenFundsPaidDate(toDate(periodStart), toDate(periodEnd), pageable);
      return PagedResponseDto.wrapSuccessResult(betweenFundsPaidDate, Constants.FETCH_SUCCESSFUL);
    }

    if (!download) {
      Page<FloatOrderPaymentReport> result = floatOrderPaymentReportService.findAll(pageable);
      return PagedResponseDto.wrapSuccessResult(result, Constants.FETCH_SUCCESSFUL);
    }

    return Helper.failedResponse("FAILED TO GENERATE REPORT");
  }


  @GetMapping("/res/stores/grnReport")
  public ResponseEntity<?> getGRNReportFile(
      @RequestParam(required = false, defaultValue = "false") boolean download,
      @RequestParam(required = false) String supplier,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize)
      throws GeneralException {

    if (periodStart == null || periodEnd == null) {
      return Helper.failedResponse(Constants.REPORT_GENERATION_FAILED);
    }
    Date start = toDate(periodStart);
    Date end   = toDate(periodEnd);

    if (download) {
      InputStreamResource file =
          new InputStreamResource(excelService.createGRNDataSheet(start, end));
      String filename = "grn_report_" + UUID.randomUUID().toString().substring(7) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
          .body(file);
    }

    Page<GrnReport> result = supplier != null
        ? grnReportService.findBySupplier(pageNo, pageSize, start, end, supplier)
        : grnReportService.findBetweenDate(pageNo, pageSize, start, end);

    return PagedResponseDto.wrapSuccessResult(result, Constants.FETCH_SUCCESSFUL);
  }

  // -------------------------------------------------------------------------
  // LPO-focused analytical reports
  // -------------------------------------------------------------------------

  @GetMapping("/res/reports/lpo/funnel")
  public ResponseEntity<?> getLpoFunnel() {
    return lpoReportService.getFunnel()
            .map(f -> ResponseEntity.ok().body((Object) f))
            .orElse(ResponseEntity.noContent().build());
  }

  @GetMapping("/res/reports/lpo/aging")
  public ResponseEntity<?> getLpoAging() {
    return ResponseEntity.ok().body(lpoReportService.getLpoAging());
  }

  @GetMapping("/res/reports/lpo/spendByCategory")
  public ResponseEntity<?> getSpendByCategory() {
    return ResponseEntity.ok().body(lpoReportService.getSpendByCategory());
  }

  @GetMapping("/res/reports/lpo/supplierAwardRate")
  public ResponseEntity<?> getSupplierAwardRate() {
    return ResponseEntity.ok().body(lpoReportService.getSupplierAwardRates());
  }

  @GetMapping("/res/reports/lpo/spendByDepartment")
  public ResponseEntity<?> getSpendByDepartment() {
    return ResponseEntity.ok().body(lpoReportService.getSpendByDepartment());
  }

  @GetMapping("/res/reports/lpo/supplierSpend")
  public ResponseEntity<?> getSupplierSpend() {
    return ResponseEntity.ok().body(lpoReportService.getSupplierSpend());
  }

  @GetMapping("/res/reports/lpo/pendingApprovals")
  public ResponseEntity<PendingApprovalsDto> getPendingApprovals() {
    return ResponseEntity.ok().body(lpoReportService.getPendingApprovals());
  }

  @GetMapping("/res/reports/lpo/cycleTime")
  public ResponseEntity<?> getCycleTime() {
    return ResponseEntity.ok().body(lpoReportService.getCycleTime());
  }

  @GetMapping("/res/reports/lpo/monthlyTrends")
  public ResponseEntity<?> getMonthlyTrends(
          @RequestParam(defaultValue = "6") int months) {
    return ResponseEntity.ok().body(lpoReportService.getMonthlyTrends(months));
  }

  @GetMapping("/res/reports/lpo/cancellationRate")
  public ResponseEntity<?> getCancellationRate() {
    return ResponseEntity.ok().body(lpoReportService.getCancellationRate());
  }
}
