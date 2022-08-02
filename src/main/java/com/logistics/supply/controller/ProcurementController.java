package com.logistics.supply.controller;

import com.logistics.supply.dto.MappingSuppliersAndRequestItemsDTO;
import com.logistics.supply.dto.RequestItemDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.Supplier;
import com.logistics.supply.service.ProcurementService;
import com.logistics.supply.service.RequestItemService;
import com.logistics.supply.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;

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
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER')")
  public ResponseEntity<?> addSuppliersToRequestItem(
      @RequestBody MappingSuppliersAndRequestItemsDTO mappingDTO) {

    Set<RequestItem> items =
        mappingDTO.getRequestItems().stream()
            .filter(i -> requestItemService.existById(i.getId()))
            .map(r -> requestItemService.findById(r.getId()).get())
            .collect(Collectors.toSet());

    Set<Supplier> suppliers =
        mappingDTO.getSuppliers().stream()
            .map(s -> supplierService.findById(s.getId()))
            .collect(Collectors.toSet());

    Set<RequestItemDTO> mappedRequests = procurementService.assignRequestToSupplier(suppliers, items);

    if (!mappedRequests.isEmpty()) {
      return ResponseDTO.wrapSuccessResult(mappedRequests, "UPDATE SUCCESSFUL");
    }
    return ResponseDTO.wrapErrorResult("UPDATE FAILED");
  }

  @GetMapping(value = "/procurement/endorsedItemsWithMultipleSuppliers")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> findEndorsedItemsWithMultipleSuppliers() {
    List<RequestItem> endorsedItemsWithAssignedSuppliers =
        requestItemService.getEndorsedItemsWithAssignedSuppliers();
    return ResponseDTO.wrapSuccessResult(endorsedItemsWithAssignedSuppliers, FETCH_SUCCESSFUL);
  }

  @GetMapping(value = "/procurement/endorsedItemsWithSupplierId/suppliers/{supplierId}")
  @PreAuthorize("hasRole('ROLE_PROCUREMENT_OFFICER') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> findRequestItemsBySupplierId(
      @PathVariable("supplierId") int supplierId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      Set<RequestItem> requestItemsForSupplier =
          requestItemService.findRequestItemsForSupplier(supplierId);
      return ResponseDTO.wrapSuccessResult(requestItemsForSupplier, FETCH_SUCCESSFUL);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return ResponseDTO.wrapErrorResult("FETCH FAILED");
  }

  @Operation(
      summary = "Generate a PDF with the list of request assigned to a supplier",
      tags = "PROCUREMENT")
  @GetMapping(value = "procurement/generateRequestListForSupplier/suppliers/{supplierId}")
  public void generateRequestListForSupplier(
      @PathVariable("supplierId") int supplierId, HttpServletResponse response) {
    try {

      File file = requestItemService.generateRequestListForSupplier(supplierId);
      if (Objects.isNull(file)) log.error("Error while generating Request list file");

      String mimeType = URLConnection.guessContentTypeFromName(file.getName());
      if (mimeType == null) {
        mimeType = "application/octet-stream";
      }
      response.setContentType(mimeType);
      response.setHeader(
          "Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

      response.setContentLength((int) file.length());

      InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

      FileCopyUtils.copy(inputStream, response.getOutputStream());
    } catch (Exception e) {
      log.error(e.toString());
    }
  }
}
