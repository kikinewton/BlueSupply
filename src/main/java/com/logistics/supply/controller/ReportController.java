package com.logistics.supply.controller;

import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static com.logistics.supply.util.Helper.failedResponse;

@RestController
@Slf4j
@RequestMapping(value = "/api")
@CrossOrigin(
        origins = {
                "https://etornamtechnologies.github.io/skyblue-request-frontend-react",
                "http://localhost:4000"
        },
        allowedHeaders = "*")
public class ReportController extends AbstractRestService {

  @GetMapping("/procurement/procuredItemsReport/download")
  public ResponseEntity<Resource> getFile(
          @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                  Date periodStart,
          @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                  Date periodEnd)
      throws IOException {

    InputStreamResource file =
        new InputStreamResource(excelService.createProcuredItemsDataSheet(periodStart, periodEnd));

    UUID u = UUID.randomUUID();
    String filename = "items_report_" + u.toString().substring(7) + ".xlsx";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .body(file);
  }

  @GetMapping("/accounts/paymentReport/download")
  public ResponseEntity<Resource> getPaymentReportFile(
          @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodStart,
          @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodEnd)
      throws IOException {

    InputStreamResource file =
        new InputStreamResource(excelService.createPaymentDataSheet(periodStart, periodEnd));

    UUID u = UUID.randomUUID();
    String filename = "payments_report_" + u.toString().substring(7) + ".xlsx";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(file);
  }

  @GetMapping("/accounts/pettyCashPaymentReport/download")
  public ResponseEntity<Resource> getPettyCashPaymentReportFile(
          @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
          @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd)
          throws IOException {

    InputStreamResource file =
            new InputStreamResource(excelService.createPettyCashPaymentDataSheet(periodStart, periodEnd));

    UUID u = UUID.randomUUID();
    String filename = "petty_cash_payments_report_" + u.toString().substring(7) + ".xlsx";
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
  }

  @GetMapping("/accounts/floatAgeingAnalysisReport/download")
  public ResponseEntity<?> getFloatAgeingAnalysisReportFile()
          throws IOException {
    try{
      InputStreamResource file =
              new InputStreamResource(excelService.createFloatAgingAnalysis());

      UUID u = UUID.randomUUID();
      String filename = "float_ageing_analysis" + u.toString().substring(7) + ".xlsx";
      return ResponseEntity.ok()
              .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
              //        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
              .contentType(MediaType.APPLICATION_OCTET_STREAM)
              .body(file);
    }
    catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("FAILED_TO GENERATE_REPORT");

  }

  @GetMapping("/stores/grn/download")
  public ResponseEntity<Resource> getGRNReportFile(
      @RequestParam(required = false) LocalDate periodStart,
      @RequestParam(required = false) LocalDate periodEnd)
      throws IOException {

    InputStreamResource file =
        new InputStreamResource(excelService.createGRNDataSheet(periodStart, periodEnd));

    UUID u = UUID.randomUUID();
    String filename = "grn_report_" + u.toString().substring(7) + ".xlsx";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .body(file);
  }


}
