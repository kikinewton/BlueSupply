package com.logistics.supply.controller;

import com.logistics.supply.dto.PeriodDTO;
import com.logistics.supply.service.AbstractRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

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
      @RequestParam(required = false) long periodStart,
      @RequestParam(required = false) long periodEnd)
      throws IOException {
    if (Objects.isNull(periodStart)) periodStart = System.currentTimeMillis();
    Date startDate = new java.util.Date(periodStart);
    System.out.println("startDate = " + startDate);
    if (Objects.isNull(periodEnd)) periodEnd = System.currentTimeMillis();
    Date endDate = new java.util.Date(periodEnd);
    System.out.println("endDate = " + endDate);
    InputStreamResource file =
        new InputStreamResource(excelService.createProcuredItemsDataSheet(startDate, endDate));

    UUID u = UUID.randomUUID();
    String filename = "items_report_" + u + ".xlsx";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .body(file);
  }

  @GetMapping("/accounts/paymentReport/download")
  public ResponseEntity<Resource> getPaymentReportFile(
      @RequestParam(required = false) long periodStart,
      @RequestParam(required = false) long periodEnd)
      throws IOException {
    if (Objects.isNull(periodStart)) periodStart = System.currentTimeMillis();
    Date startDate = new java.util.Date(periodStart);
    System.out.println("startDate = " + startDate);
    if (Objects.isNull(periodEnd)) periodEnd = System.currentTimeMillis();
    Date endDate = new java.util.Date(periodEnd);
    System.out.println("endDate = " + endDate);
    InputStreamResource file =
        new InputStreamResource(excelService.createPaymentDataSheet(startDate, endDate));

    UUID u = UUID.randomUUID();
    String filename = "payments_report_" + u + ".xlsx";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        //        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(file);
  }

  @GetMapping("/stores/grn/download")
  public ResponseEntity<Resource> getGRNReportFile(
      @RequestParam(required = false) long periodStart,
      @RequestParam(required = false) long periodEnd)
      throws IOException {
    if (Objects.isNull(periodStart)) periodStart = System.currentTimeMillis();
    Date startDate = new java.util.Date(periodStart);
    System.out.println("startDate = " + startDate);
    if (Objects.isNull(periodEnd)) periodEnd = System.currentTimeMillis();
    Date endDate = new java.util.Date(periodEnd);
    System.out.println("endDate = " + endDate);
    InputStreamResource file =
        new InputStreamResource(excelService.createGRNDataSheet(startDate, endDate));

    UUID u = UUID.randomUUID();
    String filename = "grn_report_" + u + ".xlsx";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
        .body(file);
  }
}
