package com.logistics.supply.controller;

import com.logistics.supply.dto.MappingSuppliersAndRequestItemsDto;
import com.logistics.supply.dto.RequestItemDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.service.ProcurementService;
import com.logistics.supply.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URLConnection;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@Validated
@CrossOrigin(
    origins = {
      "https://etornamtechnologies.github.io/skyblue-request-frontend-react",
      "http://localhost:4000"
    },
    allowedHeaders = "*")
@RequiredArgsConstructor
public class ProcurementController {

  private final ProcurementService procurementService;

  @Operation(summary = "Assign selected suppliers to endorsed request items", tags = "PROCUREMENT")
  @PutMapping(value = "/api/procurement/assignSuppliers/requestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<ResponseDto<Set<RequestItemDto>>> addSuppliersToRequestItem(
      @Valid @RequestBody MappingSuppliersAndRequestItemsDto mappingDto) {

    Set<RequestItemDto> mappedRequests = procurementService.assignRequestToSupplier(mappingDto);
    return ResponseDto.wrapSuccessResult(mappedRequests, "UPDATE SUCCESSFUL");
  }

  @GetMapping(value = "/api/procurement/endorsedItemsWithMultipleSuppliers")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<ResponseDto<List<RequestItem>>> findEndorsedItemsWithMultipleSuppliers() {

    List<RequestItem> endorsedItemsWithAssignedSuppliers =
        procurementService.getEndorsedItemsWithAssignedSuppliers();
    return ResponseDto.wrapSuccessResult(endorsedItemsWithAssignedSuppliers, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/api/procurement/endorsedItemsWithSupplierId/suppliers/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<ResponseDto<Collection<RequestItem>>> findRequestItemsBySupplierId(
      @PathVariable("supplierId") int supplierId) {

      Set<RequestItem> requestItemsForSupplier =
          procurementService.findUnprocessedRequestItemsForSupplier(supplierId);
      return ResponseDto.wrapSuccessResult(requestItemsForSupplier, Constants.FETCH_SUCCESSFUL);
  }

  @Operation(
      summary = "Generate a PDF with the list of request assigned to a supplier",
      tags = "PROCUREMENT")
  @GetMapping(value = "/res/procurement/generateRequestListForSupplier/suppliers/{supplierId}")
  public void generateRequestListFileForSupplier(
          @PathVariable("supplierId") int supplierId,
          HttpServletResponse response) {

    File itemsForSupplierFile =
            procurementService.generateRequestListForSupplier(supplierId);

      String mimeType = URLConnection.guessContentTypeFromName(itemsForSupplierFile.getName());
      if (mimeType == null) {
        mimeType = "application/octet-stream";
      }
      response.setContentType(mimeType);
      response.setHeader("Content-Disposition",
              String.format("inline; filename=\"%s\"", itemsForSupplierFile.getName()));

      response.setContentLength((int) itemsForSupplierFile.length());

      try (InputStream inputStream = new BufferedInputStream(new FileInputStream(itemsForSupplierFile))) {
        FileCopyUtils.copy(inputStream, response.getOutputStream());
      } catch (IOException e) {
        log.error("Error while generating request list for supplier id {}: {}", supplierId, e.getMessage());
      }
  }

}
