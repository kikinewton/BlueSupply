package com.logistics.supply.controller;

import com.logistics.supply.dto.PagedResponseDTO;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@RestController
@Slf4j
@RequestMapping(value = "/api")
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

  @GetMapping("/procurement/procuredItemsReport")
  public ResponseEntity<?> getFile(
      @RequestParam(required = false) Optional<Boolean> download,
      @RequestParam(required = false) Optional<String> supplier,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> periodStart,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize)
      throws IOException {

    if (periodStart.isPresent() && periodEnd.isPresent() && supplier.isPresent()) {
      Page<ProcuredItemReport> procured =
          procuredItemReportService.findBySupplier(
              pageNo, pageSize, periodStart.get(), periodEnd.get(), supplier.get());
      if (procured != null) {
        return pagedProcuredResult(procured);
      } else return notFound("NOT_FOUND");
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
      if (procured != null) {
        return pagedProcuredResult(procured);
      } else return notFound("NOT_FOUND");
    }

    return failedResponse("FAILED_TO GENERATE_REPORT");
  }

  @GetMapping("/accounts/paymentReport")
  public ResponseEntity<?> getPaymentReportFile(
      @RequestParam(required = false) Optional<Boolean> download,
      @RequestParam(required = false) Optional<String> supplier,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> periodStart,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> periodEnd)
      throws IOException {

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
      if (paymentReports != null) {
        return pagedPaymentResult(paymentReports);
      } else return notFound("NOT_FOUND");
    }

    if (periodStart.isPresent() && periodEnd.isPresent()) {
      Page<PaymentReport> paymentReports =
          paymentReportService.findBetweenDate(
              pageNo, pageSize, periodStart.get(), periodEnd.get());
      if (paymentReports != null) {
        return pagedPaymentResult(paymentReports);
      } else return notFound("NOT_FOUND");
    }

    return failedResponse("FAILED_TO GENERATE_REPORT");
  }

  @GetMapping("/accounts/pettyCashPaymentReport")
  public ResponseEntity<?> getPettyCashPaymentReportFile(
      @RequestParam(required = false) Optional<Boolean> download,
      @RequestParam(required = false) Optional<String> requesterEmail,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> periodStart,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize)
      throws IOException {

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
      if (ptc != null) {
        return pagedPettyCashResult(ptc);
      } else return notFound("NOT_FOUND");
    }

    if (periodStart.isPresent() && periodEnd.isPresent()) {
      Page<PettyCashPaymentReport> ptc =
          pettyCashPaymentReportService.findBetweenDate(
              pageNo, pageSize, periodStart.get(), periodEnd.get());
      System.out.println("ptc = " + ptc);
      if (ptc != null) {
        return pagedPettyCashResult(ptc);
      } else return notFound("NOT_FOUND");
    }

    return failedResponse("FAILED_TO GENERATE_REPORT");
  }

  @GetMapping("/accounts/floatAgeingAnalysisReport")
  public ResponseEntity<?> getFloatAgeingAnalysisReportFile(
      @RequestParam(required = false) Optional<Boolean> download,
      @RequestParam(required = false) Optional<String> requesterEmail,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          Optional<Date> periodStart,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
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
          if (requesterFloatAA != null) {
            return pagedResult(requesterFloatAA);
          }
        } catch (Exception e) {
          log.error(e.toString());
        }
      }

      if (periodStart.isPresent() && periodEnd.isPresent()) {
        Page<FloatAgingAnalysis> res =
            floatAgeingAnalysisService.findBetweenDate(
                pageNo, pageSize, periodStart.get(), periodEnd.get());
        if (res != null) {
          return pagedResult(res);
        }
      }

      if (!download.isPresent()) {
        Page<FloatAgingAnalysis> result =
            floatAgeingAnalysisService.findAllFloatAnalysis(pageNo, pageSize);
        if (result != null) {
          return pagedResult(result);
        } else return notFound("NOT_FOUND");
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("FAILED_TO GENERATE_REPORT");
  }

  @GetMapping("/stores/grnReport")
  public ResponseEntity<?> getGRNReportFile(
      @RequestParam(required = false) Optional<Boolean> download,
      @RequestParam(required = false) Optional<String> supplier,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> periodStart,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> periodEnd,
      @RequestParam(defaultValue = "0") int pageNo,
      @RequestParam(defaultValue = "200") int pageSize)
      throws IOException {

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
      if (result != null) {
        return pagedGrnResult(result);
      } else return notFound("NOT_FOUND");
    }

    if (periodStart.isPresent() && periodEnd.isPresent()) {
      Page<GrnReport> result =
          grnReportService.findBetweenDate(pageNo, pageSize, periodStart.get(), periodEnd.get());
      if (result != null) {
        return pagedGrnResult(result);
      } else return notFound("NOT_FOUND");
    }
    return failedResponse("FAILED_TO GENERATE_REPORT");
  }

  private ResponseEntity<?> pagedProcuredResult(Page<ProcuredItemReport> procured) {
    PagedResponseDTO.MetaData metaData =
        new PagedResponseDTO.MetaData(
            procured.getNumberOfElements(),
            procured.getPageable().getPageSize(),
            procured.getNumber(),
            procured.getTotalPages());
    PagedResponseDTO response =
        new PagedResponseDTO("FETCH_SUCCESSFUL", SUCCESS, metaData, procured.getContent());
    return ResponseEntity.ok(response);
  }

  private ResponseEntity<?> pagedGrnResult(Page<GrnReport> grn) {
    PagedResponseDTO.MetaData metaData =
        new PagedResponseDTO.MetaData(
            grn.getNumberOfElements(),
            grn.getPageable().getPageSize(),
            grn.getNumber(),
            grn.getTotalPages());
    PagedResponseDTO response =
        new PagedResponseDTO("FETCH_SUCCESSFUL", SUCCESS, metaData, grn.getContent());
    return ResponseEntity.ok(response);
  }

  private ResponseEntity<?> pagedPaymentResult(Page<PaymentReport> payment) {
    PagedResponseDTO.MetaData metaData =
        new PagedResponseDTO.MetaData(
            payment.getNumberOfElements(),
            payment.getPageable().getPageSize(),
            payment.getNumber(),
            payment.getTotalPages());
    PagedResponseDTO response =
        new PagedResponseDTO("FETCH_SUCCESSFUL", SUCCESS, metaData, payment.getContent());
    return ResponseEntity.ok(response);
  }

  private ResponseEntity<?> pagedResult(Page<FloatAgingAnalysis> floats) {
    PagedResponseDTO.MetaData metaData =
        new PagedResponseDTO.MetaData(
            floats.getNumberOfElements(),
            floats.getPageable().getPageSize(),
            floats.getNumber(),
            floats.getTotalPages());
    PagedResponseDTO response =
        new PagedResponseDTO("FETCH_SUCCESSFUL", SUCCESS, metaData, floats.getContent());
    return ResponseEntity.ok(response);
  }

  private ResponseEntity<?> pagedPettyCashResult(Page<PettyCashPaymentReport> ptc) {
    PagedResponseDTO.MetaData metaData =
        new PagedResponseDTO.MetaData(
            ptc.getNumberOfElements(),
            ptc.getPageable().getPageSize(),
            ptc.getNumber(),
            ptc.getTotalPages());
    PagedResponseDTO response =
        new PagedResponseDTO("FETCH_SUCCESSFUL", SUCCESS, metaData, ptc.getContent());
    return ResponseEntity.ok(response);
  }
}
