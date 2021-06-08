package com.logistics.supply.service;

import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.logistics.supply.enums.EndorsementStatus.ENDORSED;
import static com.logistics.supply.enums.EndorsementStatus.REJECTED;
import static com.logistics.supply.enums.RequestApproval.APPROVED;
import static com.logistics.supply.enums.RequestStatus.*;

@Service
@Slf4j
@Transactional
public class RequestItemService extends AbstractDataService {

  public List<RequestItem> findAll(int pageNo, int pageSize) {
    Pageable paging = PageRequest.of(pageNo, pageSize);
    List<RequestItem> requestItemList = new ArrayList<>();
    try {
      Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdDate"));
      Page<RequestItem> items = requestItemRepository.findAll(pageable);
      items.forEach(requestItemList::add);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return requestItemList;
  }

  public RequestItem create(RequestItem item) {
    try {
      return requestItemRepository.save(item);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public List<RequestItem> getCountNofEmployeeRequest(int count, int employeeId) {
    return requestItemRepository.getEmployeeRequest(employeeId).stream()
        .limit(count)
        .collect(Collectors.toList());
  }

  public Optional<RequestItem> findById(int requestItemId) {
    Optional<RequestItem> requestItem = null;
    try {
      requestItem = requestItemRepository.findById(requestItemId);
      return requestItem;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public boolean supplierIsPresent(RequestItem requestItem, Supplier supplier) {

    requestItem = requestItemRepository.findById(requestItem.getId()).get();
    System.out.println("requestItem id= " + requestItem.getId().toString());
    Set<Supplier> suppliers =
        requestItem.getSuppliers().stream()
            .map(s -> supplierRepository.findById(s.getId()).get())
            .collect(Collectors.toSet());
    System.out.println("Supplier is present with size: " + suppliers.size());
    suppliers.forEach(System.out::println);
    for (Supplier s : suppliers) {
      if (s.getId() == supplier.getId()) return true;
    }
    return false;
  }

  public List<RequestItem> getRequestDateGreaterThan(String date) {
    List<RequestItem> itemList = new ArrayList<>();
    try {
      List<RequestItem> requestItemList = requestItemRepository.getRequestBetweenDateAndNow(date);
      requestItemList.forEach(itemList::add);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return itemList;
  }

  public List<RequestItem> findByStatusAndStartDate(String status, String startDate) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getByStatus(status, startDate));
      return items;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return items;
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem endorseRequest(int requestItemId) {
    Optional<RequestItem> requestItem = findById(requestItemId);
    if (requestItem.isPresent()) {
      requestItem.get().setEndorsement(ENDORSED);
      requestItem.get().setEndorsementDate(new Date());
      try {
        RequestItem result = requestItemRepository.save(requestItem.get());
        if (Objects.nonNull(result)) {
          return result;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public boolean approveRequest(int requestItemId) {
    Optional<RequestItem> requestItem = findById(requestItemId);
    if (requestItem.isPresent()
        && requestItem.get().getEndorsement().equals(ENDORSED)
        && requestItem.get().getStatus().equals(RequestStatus.PENDING)) {
      requestItem.get().setApproval(APPROVED);
      //      requestItem.get().setStatus(PROCESSED);
      requestItem.get().setApprovalDate(new Date());
      try {
        RequestItem result = requestItemRepository.save(requestItem.get());
        if (Objects.nonNull(result)) {
          return true;
        }
      } catch (Exception e) {
        log.error(e.getMessage());
        e.printStackTrace();
      }
    }
    return false;
  }

  @Transactional(rollbackFor = Exception.class)
  public CancelledRequestItem cancelRequest(int requestItemId, int employeeId) {
    System.out.println("Cancel process initialised");
    Optional<Employee> employee = employeeRepository.findById(employeeId);

    if (employee.isPresent()) {

      Optional<RequestItem> requestItem = findById(requestItemId);
      if (requestItem.isPresent() && requestItem.get().getStatus().equals(RequestStatus.PENDING)) {
        System.out.println("Cancel Request is valid");
        int deptId = requestItem.get().getEmployee().getDepartment().getId();
        Employee emp =
            employeeRepository.findDepartmentHod(deptId, EmployeeRole.ROLE_HOD.ordinal());
        requestItem.get().setEndorsement(REJECTED);
        requestItem.get().setEndorsementDate(new Date());
        requestItem.get().setApproval(RequestApproval.REJECTED);
        requestItem.get().setApprovalDate(new Date());
        if (emp.getId().equals(employee.get().getId())) {
          requestItem.get().setStatus(ENDORSEMENT_CANCELLED);
        } else {
          LocalPurchaseOrder lpo = localPurchaseOrderRepository.findLpoByRequestItem(requestItemId);
          if (lpo.getRequestItems().size() == 1) {
            lpo.setComment("Request approval cancelled");
          }

          requestItem.get().setStatus(APPROVAL_CANCELLED);
        }
        RequestItem result = requestItemRepository.save(requestItem.get());
        if (Objects.nonNull(result)) {
          return saveRequest(result, employee.get(), result.getStatus());
        }
      }
    }
    return null;
  }

  public List<RequestItem> getEndorsedItems() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getEndorsedRequestItems());
      return items;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return items;
  }

  public Optional<RequestItem> findApprovedItemById(int requestItemId) {
    return requestItemRepository.findApprovedRequestById(requestItemId);
  }

  public List<RequestItem> getApprovedItems() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getApprovedRequestItems());
      return items;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return items;
  }

  public CancelledRequestItem saveRequest(
      RequestItem requestItem, Employee employee, RequestStatus status) {
    CancelledRequestItem request = new CancelledRequestItem();
    request.setRequestItem(requestItem);
    request.setStatus(status);
    request.setEmployee(employee);
    try {
      return cancelledRequestItemRepository.save(request);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public List<RequestItem> getRequestItemForHOD(int departmentId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getRequestItemForHOD(departmentId));
      return items;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return items;
  }

  public List<RequestItem> getRequestItemForGeneralManager() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getRequestItemsForGeneralManager());
      return items;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem assignSuppliersToRequestItem(
      RequestItem requestItem, Set<Supplier> suppliers) {
    requestItem.setSuppliers(suppliers);
    return requestItemRepository.save(requestItem);
  }

  //  public RequestItem assignRequestItemToSupplier(RequestItem requestItem, Supplier supplier) {
  //
  //  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItem assignRequestCategory(int requestItemId, RequestCategory requestCategory) {
    RequestItem requestItem = findById(requestItemId).get();
    requestItem.setRequestCategory(requestCategory);
    return requestItemRepository.save(requestItem);
  }

  public List<RequestItem> getEndorsedItemsWithAssignedSuppliers() {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getEndorsedRequestItemsWithSuppliersAssigned());
      return items;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return items;
  }

  public List<RequestItem> findRequestItemsWithoutDocInQuotation() {
    List<RequestItem> items = new ArrayList<>();
    try {
      List<Integer> ids = requestItemRepository.findItemIdWithoutDocsInQuotation();
      if (ids.size() > 0) {
        items.addAll(
            ids.stream()
                .map(x -> requestItemRepository.findById(x).get())
                .collect(Collectors.toList()));
        return items;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return items;
  }

  public List<RequestItem> getEndorsedRequestItemsForDepartment(int departmentId) {
    List<RequestItem> items = new ArrayList<>();
    try {
      items.addAll(requestItemRepository.getDepartmentEndorsedRequestItemForHOD(departmentId));
      return items;
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }
}
