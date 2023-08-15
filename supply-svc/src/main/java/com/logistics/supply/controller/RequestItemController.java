package com.logistics.supply.controller;

import com.logistics.supply.dto.ItemUpdateDto;
import com.logistics.supply.dto.PagedResponseDto;
import com.logistics.supply.dto.RequestItemDto;
import com.logistics.supply.dto.ResponseDto;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.TrackRequestDTO;
import com.logistics.supply.service.EmployeeService;
import com.logistics.supply.service.RequestItemService;
import com.logistics.supply.service.TrackRequestStatusService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.logistics.supply.util.Constants.FETCH_SUCCESSFUL;
import static org.springframework.data.domain.Sort.by;

@RestController
@RequestMapping(value = "/api")
@CrossOrigin(
        origins = {
                "https://etornamtechnologies.github.io/skyblue-request-frontend-react",
                "http://localhost:4000"
        },
        allowedHeaders = "*")
@RequiredArgsConstructor
public class RequestItemController {

    private final RequestItemService requestItemService;
    private final EmployeeService employeeService;
    private final TrackRequestStatusService trackRequestStatusService;

    @GetMapping(value = "/requestItems")
    @PreAuthorize(
            "hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_ADMIN') or hasRole('ROLE_PROCUREMENT_MANAGER')")
    public ResponseEntity<PagedResponseDto<Page<RequestItem>>> listRequestItems(
            @RequestParam(defaultValue = "0", required = false) int pageNo,
            @RequestParam(defaultValue = "300", required = false) int pageSize,
            @RequestParam(required = false, defaultValue = "false") Optional<Boolean> toBeApproved,
            @RequestParam(required = false, defaultValue = "false") Boolean approved,
            @RequestParam(required = false, name = "requestItemName") String requestItemName) {

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());

        if (approved) {

            Page<RequestItem> approvedItems = requestItemService.getApprovedItems(pageable);

            return PagedResponseDto.wrapSuccessResult(
                    approvedItems,
                    FETCH_SUCCESSFUL);
        }
        if (toBeApproved.isPresent() && toBeApproved.get()) {

            Page<RequestItem> endorsedItemsWithAssignedSuppliers = requestItemService
                    .getEndorsedItemsWithAssignedSuppliers(pageable);

            return PagedResponseDto.wrapSuccessResult(
                    endorsedItemsWithAssignedSuppliers,
                    FETCH_SUCCESSFUL);
        }

        if (StringUtils.hasText(requestItemName)) {
            Page<RequestItem> requestItemMatchingName = requestItemService
                    .findByRequestItemName(requestItemName.trim(), pageable);
            return PagedResponseDto.wrapSuccessResult(requestItemMatchingName, FETCH_SUCCESSFUL);
        }

        Page<RequestItem> data = requestItemService.findAll(pageNo, pageSize);

        return PagedResponseDto.wrapSuccessResult(data, FETCH_SUCCESSFUL);
    }

    @GetMapping(value = "/requestItems/{requestItemId}")
    public ResponseEntity<ResponseDto<RequestItem>> getRequestItemById(@PathVariable int requestItemId) {

        RequestItem requestItem = requestItemService.findById(requestItemId);
        return ResponseDto.wrapSuccessResult(requestItem, FETCH_SUCCESSFUL);
    }

    @GetMapping(value = "/requestItemsByDepartment")
    @PreAuthorize("hasRole('ROLE_HOD')")
    public ResponseEntity<ResponseDto<List<RequestItemDto>>> listRequestItemsByDepartment(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "false") Optional<Boolean> toBeReviewed) {

        Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
        if (toBeReviewed.isPresent() && toBeReviewed.get()) {
            List<RequestItemDto> requestItemsDtoToBeReviewed =
                    requestItemService.findRequestItemsDtoToBeReviewed(
                            RequestReview.PENDING, employee.getDepartment().getId());
            return ResponseDto.wrapSuccessResult(requestItemsDtoToBeReviewed, FETCH_SUCCESSFUL);
        }

        List<RequestItemDto> items =
                requestItemService.getRequestItemForHOD(employee.getDepartment().getId());
        return ResponseDto.wrapSuccessResult(items, FETCH_SUCCESSFUL);
    }

    @Operation(
            summary =
                    "Fetch endorsed items for department by HOD, with params get the request_items " +
                    "with assigned final supplier")
    @GetMapping(value = "/requestItemsByDepartment/endorsed")
    @PreAuthorize("hasRole('ROLE_HOD')")
    public ResponseEntity<ResponseDto<List<RequestItem>>> listEndorsedRequestItemsForDepartment(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "false") Optional<Boolean> review,
            @RequestParam(required = false) String quotationId) {

        Employee employee = employeeService.findEmployeeByEmail(authentication.getName());

        if (review.isPresent() && review.get() && Objects.nonNull(quotationId)) {
            List<RequestItem> items =
                    requestItemService.getItemsWithFinalPriceUnderQuotation(Integer.parseInt(quotationId));
            return ResponseDto.wrapSuccessResult(items, "ENDORSED ITEMS WITH PRICES FROM SUPPLIER");
        }
        List<RequestItem> items =
                requestItemService.getEndorsedRequestItemsForDepartment(employee.getDepartment().getId());
        return ResponseDto.wrapSuccessResult(items, "ENDORSED REQUEST ITEM");
    }

    @Operation(summary = "Get endorsed items for procurement to work on")
    @GetMapping("/requestItems/endorsed")
    @PreAuthorize(" hasRole('ROLE_PROCUREMENT_MANAGER') or hasRole('ROLE_PROCUREMENT_OFFICER')")
    public ResponseEntity<ResponseDto<List<RequestItem>>> listAllEndorsedRequestItems(
            @RequestParam(required = false, defaultValue = "false") Boolean withSupplier) {

        if (withSupplier) {
            List<RequestItem> endorsedItemsWithSuppliers = requestItemService.getEndorsedItemsWithSuppliers();
            return ResponseDto.wrapSuccessResult(endorsedItemsWithSuppliers, "ENDORSED REQUEST ITEMS");
        }

        List<RequestItem> endorsedItemsWithoutSuppliers = requestItemService.getEndorsedItemsWithoutSuppliers();
        return ResponseDto.wrapSuccessResult(endorsedItemsWithoutSuppliers, "ENDORSED REQUEST ITEMS");
    }

    @GetMapping(value = "/requestItemsForEmployee")
    public ResponseEntity<PagedResponseDto<Page<RequestItemDto>>> listRequestItemsForEmployee(
            Authentication authentication,
            @RequestParam(required = false) String requestItemName,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "200") int pageSize,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<Date> startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<Date> endDate) {

        Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
        Pageable pageable =
                PageRequest.of(
                        pageNo,
                        pageSize,
                        by("id").descending().and(by("priorityLevel")));

        if (StringUtils.hasText(requestItemName)) {
            Page<RequestItemDto> byEmployeeAndItemName = requestItemService
                    .findByEmployeeAndItemName(employee, requestItemName.trim(), pageable);
            return PagedResponseDto.wrapSuccessResult(byEmployeeAndItemName, FETCH_SUCCESSFUL);
        }

        if (startDate.isPresent() && endDate.isPresent()) {

            Page<RequestItemDto> requestsInDateRange = requestItemService
                    .findItemsInDateRange(
                            startDate.get(),
                            endDate.get(),
                            employee,
                            pageable);

            return PagedResponseDto.wrapSuccessResult(requestsInDateRange, FETCH_SUCCESSFUL);
        }

        Page<RequestItemDto> items = requestItemService.findByEmployee(employee, pageable);
        return PagedResponseDto.wrapSuccessResult(items, FETCH_SUCCESSFUL);
    }

    @Operation(summary = "Change quantity or name of items requested", tags = "REQUEST ITEM")
    @PutMapping(value = "/requestItems/{requestItemId}")
    public ResponseEntity<ResponseDto<RequestItem>> updateQuantityForNotEndorsedRequest(
            @PathVariable("requestItemId") int requestItemId,
            @Valid @RequestBody ItemUpdateDto itemUpdateDTO,
            Authentication authentication) {

        RequestItem result =
                requestItemService.updateItemQuantity(
                        requestItemId, itemUpdateDTO, authentication.getName());
        return ResponseDto.wrapSuccessResult(result, "ITEM UPDATE SUCCESSFUL");
    }

    @Operation(summary = "Get the list of endorsed items for department HOD")
    @GetMapping(value = "/requestItems/departmentHistory")
    @PreAuthorize("hasRole('ROLE_HOD')")
    public ResponseEntity<PagedResponseDto<Page<RequestItem>>> getRequestHistoryByDepartment(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "200") int pageSize) {

        Department department =
                employeeService.findEmployeeByEmail(authentication.getName()).getDepartment();
        Page<RequestItem> items =
                requestItemService.requestItemsHistoryByDepartment(department, pageNo, pageSize);
        return PagedResponseDto.wrapSuccessResult(items, FETCH_SUCCESSFUL);
    }

    @GetMapping("/requestItems/{requestItemId}/status")
    public ResponseEntity<ResponseDto<TrackRequestDTO>> getStatusOfRequestItem(
            @PathVariable("requestItemId") int requestItemId) {

        TrackRequestDTO result = trackRequestStatusService.getRequestStage(requestItemId);
        return ResponseDto.wrapSuccessResult(result, FETCH_SUCCESSFUL);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/requestItems/{requestItemId}/resolveComment")
    public void resolveCommentOnRequest(@PathVariable("requestItemId") int requestItemId) {

        requestItemService.resolveCommentOnRequest(requestItemId);
    }
}
