package com.logistics.supply.controller;

import com.logistics.supply.dto.MappingSuppliersAndRequestItemsDTO;
import com.logistics.supply.dto.RequestItemDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.ProcurementService;
import com.logistics.supply.service.RequestItemService;
import com.logistics.supply.service.SupplierService;
import com.logistics.supply.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
public class ProcurementController {
  private final RequestItemService requestItemService;
  private final ProcurementService procurementService;
  private final SupplierService supplierService;

  @Operation(summary = "Assign selected suppliers to endorsed request items", tags = "PROCUREMENT")
  @PutMapping(value = "/procurement/assignSuppliers/requestItems")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> addSuppliersToRequestItem(
      @RequestBody MappingSuppliersAndRequestItemsDTO mappingDTO) {

    Set<RequestItem> items =
        mappingDTO.getRequestItems().stream()
            .filter(i -> requestItemService.existById(i.getId()))
            .map(r -> requestItemService.findById(r.getId()))
            .collect(Collectors.toSet());

    Set<Supplier> suppliers =
        mappingDTO.getSuppliers().stream()
            .map(s -> supplierService.findById(s.getId()))
            .collect(Collectors.toSet());

    Set<RequestItemDto> mappedRequests = procurementService.assignRequestToSupplier(suppliers, items);

    if (!mappedRequests.isEmpty()) {
      return ResponseDto.wrapSuccessResult(mappedRequests, "UPDATE SUCCESSFUL");
    }
    return ResponseDto.wrapErrorResult("UPDATE FAILED");
  }

  @GetMapping(value = "/procurement/endorsedItemsWithMultipleSuppliers")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> findEndorsedItemsWithMultipleSuppliers() {
    List<RequestItem> endorsedItemsWithAssignedSuppliers =
        requestItemService.getEndorsedItemsWithAssignedSuppliers();
    return ResponseDto.wrapSuccessResult(endorsedItemsWithAssignedSuppliers, Constants.FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/procurement/endorsedItemsWithSupplierId/suppliers/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> findRequestItemsBySupplierId(
      @PathVariable("supplierId") int supplierId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      Set<RequestItem> requestItemsForSupplier =
          requestItemService.findRequestItemsForSupplier(supplierId);
      return ResponseDto.wrapSuccessResult(requestItemsForSupplier, Constants.FETCH_SUCCESSFUL);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return ResponseDto.wrapErrorResult("FETCH FAILED");
  }

  @Operation(
      summary = "Generate a PDF with the list of request assigned to a supplier",
      tags = "PROCUREMENT")
  @GetMapping(value = "procurement/generateRequestListForSupplier/suppliers/{supplierId}")
  public void generateRequestListFileForSupplier(
          @PathVariable("supplierId") int supplierId,
          HttpServletResponse response) {

    File itemsForSupplierFile =
            requestItemService.generateRequestListForSupplier(supplierId);

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
