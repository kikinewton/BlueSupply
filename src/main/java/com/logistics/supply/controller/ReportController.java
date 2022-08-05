package com.logistics.supply.controller;

import com.logistics.supply.dto.PagedResponseDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
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

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;
import static com.logistics.supply.util.Constants.REPORT_GENERATION_FAILED;
import static com.logistics.supply.util.Helper.failedResponse;

@RestController
@Slf4j
@CrossOrigin(
    origins = {
      "https://etornamtechnologies.github.io/skyblue-request-frontend-react",
      "http://localhost:4000"
    },
    allowedHeaders = "*")
//@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

  private final ExcelService excelService;
  private final GrnReportService grnReportService;
  private final PaymentReportService paymentReportService;
  private final ProcuredItemReportService procuredItemReportService;
  private final FloatAgeingAnalysisService floatAgeingAnalysisService;
  private final PettyCashPaymentReportService pettyCashPaymentReportService;
  private final FloatOrderPaymentReportService floatOrderPaymentReportService;

  @GetMapping("/procurement/procuredItemsReport")
  public ResponseEntity<?> getFile(
      @RequestParam(required = false) Optional<Boolean> download,
      @RequestParam(required = false) Optional<String> supplier,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodStart,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize)
      throws IOException, GeneralException {

    if (periodStart.isPresent() && periodEnd.isPresent() && supplier.isPresent()) {
      Page<ProcuredItemReport> procured =
          procuredItemReportService.findBySupplier(
              pageNo, pageSize, periodStart.get(), periodEnd.get(), supplier.get());

      return PagedResponseDTO.wrapSuccessResult(procured, FETCH_SUCCESSFUL);
    }

    if (periodStart.isPresent()
        && periodEnd.isPresent()
        && download.isPresent()
        && download.get()) {
      InputStreamResource file =
          new InputStreamResource(
              excelService.createProcuredItemsDataSheet(periodStart.get(), periodEnd.get()));

      UUID u = UUID.randomUUID();
      String filename = "items_report_" + u.toString().substring(7) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
          .body(file);
    }

    if (periodStart.isPresent() && periodEnd.isPresent()) {
      Page<ProcuredItemReport> procured =
          procuredItemReportService.findAllBetween(
              pageNo, pageSize, periodStart.get(), periodEnd.get());

      return PagedResponseDTO.wrapSuccessResult(procured, FETCH_SUCCESSFUL);
    }

    return failedResponse("FAILED TO GENERATE REPORT");
  }

  @GetMapping("/accounts/paymentReport")
  public ResponseEntity<?> getPaymentReportFile(
      @RequestParam(required = false) Optional<Boolean> download,
      @RequestParam(required = false) Optional<String> supplier,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodStart,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodEnd)
      throws IOException, GeneralException {

    if (periodStart.isPresent()
        && periodEnd.isPresent()
        && download.isPresent()
        && download.get()) {
      InputStreamResource file =
          new InputStreamResource(
              excelService.createPaymentDataSheet(periodStart.get(), periodEnd.get()));

      UUID u = UUID.randomUUID();
      String filename = "payments_report_" + u.toString().substring(7) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(file);
    }

    if (periodStart.isPresent() && periodEnd.isPresent() && supplier.isPresent()) {
      Page<PaymentReport> paymentReports =
          paymentReportService.findBySupplier(
              pageNo, pageSize, periodStart.get(), periodEnd.get(), supplier.get());

      return PagedResponseDTO.wrapSuccessResult(paymentReports, FETCH_SUCCESSFUL);
    }

    if (periodStart.isPresent() && periodEnd.isPresent()) {
      Page<PaymentReport> paymentReports =
          paymentReportService.findBetweenDate(
              pageNo, pageSize, periodStart.get(), periodEnd.get());

      return PagedResponseDTO.wrapSuccessResult(paymentReports, FETCH_SUCCESSFUL);
    }

    return failedResponse("FAILED TO GENERATE REPORT");
  }

  @GetMapping("/accounts/pettyCashPaymentReport")
  public ResponseEntity<?> getPettyCashPaymentReportFile(
      @RequestParam(required = false) Optional<Boolean> download,
      @RequestParam(required = false) Optional<String> requesterEmail,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodStart,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize)
      throws IOException, GeneralException {

    if (periodStart.isPresent()
        && periodEnd.isPresent()
        && download.isPresent()
        && download.get()) {
      InputStreamResource file =
          new InputStreamResource(
              excelService.createPettyCashPaymentDataSheet(periodStart.get(), periodEnd.get()));

      UUID u = UUID.randomUUID();
      String filename = "petty_cash_payments_report_" + u.toString().substring(7) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .body(file);
    }

    if (requesterEmail.isPresent() && periodStart.isPresent() && periodEnd.isPresent()) {
      Page<PettyCashPaymentReport> ptc =
          pettyCashPaymentReportService.findByRequestedByEmail(
              pageNo, pageSize, periodStart.get(), periodEnd.get(), requesterEmail.get());

      return PagedResponseDTO.wrapSuccessResult(ptc, FETCH_SUCCESSFUL);
    }

    if (periodStart.isPresent() && periodEnd.isPresent()) {
      Page<PettyCashPaymentReport> ptc =
          pettyCashPaymentReportService.findBetweenDate(
              pageNo, pageSize, periodStart.get(), periodEnd.get());

      return PagedResponseDTO.wrapSuccessResult(ptc, FETCH_SUCCESSFUL);
    }

    return failedResponse("FAILED TO GENERATE REPORT");
  }

  @GetMapping("/accounts/floatAgeingAnalysisReport")
  public ResponseEntity<?> getFloatAgeingAnalysisReportFile(
      @RequestParam(required = false) Optional<Boolean> download,
      @RequestParam(required = false) Optional<String> requesterEmail,
      @RequestParam(required = false) Optional<String> staffId,
      @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodStart,
      @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodEnd,
      @RequestParam(required = false) Optional<Boolean> all,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize) {
    try {

      if (download.isPresent()
          && download.get()
          && periodStart.isPresent()
          && periodEnd.isPresent()) {
        InputStreamResource file =
            new InputStreamResource(
                excelService.createFloatAgingAnalysis(periodStart.get(), periodEnd.get()));
        UUID u = UUID.randomUUID();
        String filename = "float_ageing_analysis" + u.toString().substring(7) + ".xlsx";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
      }

      if (requesterEmail.isPresent() && periodStart.isPresent() && periodEnd.isPresent()) {
        try {
          Page<FloatAgingAnalysis> requesterFloatAA =
              floatAgeingAnalysisService.findFloatAnalysisByRequesterEmail(
                  pageNo, pageSize, requesterEmail.get());

          return PagedResponseDTO.wrapSuccessResult(requesterFloatAA, FETCH_SUCCESSFUL);

        } catch (Exception e) {
          log.error(e.toString());
        }
      }

      if (staffId.isPresent() && periodStart.isPresent() && periodEnd.isPresent()) {
        try {
          Page<FloatAgingAnalysis> requesterFloatAA =
              floatAgeingAnalysisService.findFloatAnalysisByStaffId(
                  pageNo, pageSize, staffId.get());

          return PagedResponseDTO.wrapSuccessResult(requesterFloatAA, FETCH_SUCCESSFUL);

        } catch (Exception e) {
          log.error(e.toString());
        }
      }

      if (periodStart.isPresent() && periodEnd.isPresent()) {
        Page<FloatAgingAnalysis> res =
            floatAgeingAnalysisService.findBetweenDate(
                pageNo, pageSize, periodStart.get(), periodEnd.get());

        return PagedResponseDTO.wrapSuccessResult(res, FETCH_SUCCESSFUL);
      }

      if (!download.isPresent()) {
        Page<FloatAgingAnalysis> result =
            floatAgeingAnalysisService.findAllFloatAnalysis(pageNo, pageSize);

        return PagedResponseDTO.wrapSuccessResult(result, FETCH_SUCCESSFUL);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("FAILED TO GENERATE REPORT");
  }

  @GetMapping("/accounts/floatOrderPaymentReport")
  public ResponseEntity<?> getFloatOrderPaymentReport(
          @RequestParam(required = false) Optional<Boolean> download,
          @RequestParam(required = false) Optional<String> staffId,
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodStart,
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodEnd,
          @RequestParam(required = false) Optional<Boolean> all,
          @RequestParam(defaultValue = "0") int pageNo,
          @RequestParam(defaultValue = "200") int pageSize) throws GeneralException {

      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("fundsAllocatedDate").descending());
      if (download.isPresent()
              && download.get()
              && periodStart.isPresent()
              && periodEnd.isPresent()) {
        InputStreamResource file =
                new InputStreamResource(
                        excelService.createFloatOrderPaymentDataSheet(periodStart.get(), periodEnd.get()));
        UUID u = UUID.randomUUID();
        String filename = "float_order_payment_report_" + u.toString().substring(7) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
      }

      if (staffId.isPresent() && periodStart.isPresent() && periodEnd.isPresent()) {
          Page<FloatOrderPaymentReport> byRequesterStaffId = floatOrderPaymentReportService.findByRequesterStaffId(staffId.get(), pageable);
          return PagedResponseDTO.wrapSuccessResult(byRequesterStaffId, FETCH_SUCCESSFUL);
      }

      if (periodStart.isPresent() && periodEnd.isPresent()) {
        Page<FloatOrderPaymentReport> betweenFundsPaidDate = floatOrderPaymentReportService.findBetweenFundsPaidDate(periodStart.get(), periodEnd.get(), pageable);
        return PagedResponseDTO.wrapSuccessResult(betweenFundsPaidDate, FETCH_SUCCESSFUL);
      }

      if (!download.isPresent()) {
        Page<FloatOrderPaymentReport> result = floatOrderPaymentReportService.findAll(pageable);
        return PagedResponseDTO.wrapSuccessResult(result, FETCH_SUCCESSFUL);
      }

    return failedResponse("FAILED TO GENERATE REPORT");
  }


  @GetMapping("/stores/grnReport")
  public ResponseEntity<?> getGRNReportFile(
      @RequestParam(required = false) Optional<Boolean> download,
      @RequestParam(required = false) Optional<String> supplier,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodStart,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
          Optional<Date> periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize)
      throws GeneralException {

    if (download.isPresent()
        && periodStart.isPresent()
        && periodEnd.isPresent()
        && download.get()) {
      InputStreamResource file =
          new InputStreamResource(
              excelService.createGRNDataSheet(periodStart.get(), periodEnd.get()));

      UUID u = UUID.randomUUID();
      String filename = "grn_report_" + u.toString().substring(7) + ".xlsx";
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
          .body(file);
    }
    if (periodStart.isPresent() && periodEnd.isPresent() && supplier.isPresent()) {
      Page<GrnReport> result =
          grnReportService.findBySupplier(
              pageNo, pageSize, periodStart.get(), periodEnd.get(), supplier.get());

      return PagedResponseDTO.wrapSuccessResult(result, FETCH_SUCCESSFUL);
    }

    if (periodStart.isPresent() && periodEnd.isPresent()) {
      Page<GrnReport> result =
          grnReportService.findBetweenDate(pageNo, pageSize, periodStart.get(), periodEnd.get());

      return PagedResponseDTO.wrapSuccessResult(result, FETCH_SUCCESSFUL);
    }
    return failedResponse(REPORT_GENERATION_FAILED);
  }
}
