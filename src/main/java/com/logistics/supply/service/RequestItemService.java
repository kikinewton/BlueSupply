package com.logistics.supply.service;

import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Request;
import com.logistics.supply.model.RequestItem;
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
import static com.logistics.supply.util.Constants.*;

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

  public String endorseRequest(int requestItemId) {
    Optional<RequestItem> requestItem = findById(requestItemId);
    if (requestItem.isPresent()) {
      requestItem.get().setEndorsement(ENDORSED);
      requestItem.get().setEndorsementDate(new Date());
      try {
        RequestItem result = requestItemRepository.save(requestItem.get());
        if (Objects.nonNull(result)) {
          return REQUEST_ENDORSED;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return REQUEST_ENDORSEMENT_DENIED;
  }

  public String approveRequest(int requestItemId) {
    Optional<RequestItem> requestItem = findById(requestItemId);
    if (requestItem.isPresent()
        && requestItem.get().getEndorsement().equals(ENDORSED)
        && requestItem.get().getStatus().equals(RequestStatus.PENDING)) {
      requestItem.get().setApproval(APPROVED);
      requestItem.get().setStatus(PROCESSED);
      requestItem.get().setApprovalDate(new Date());
      try {
        RequestItem result = requestItemRepository.save(requestItem.get());
        if (Objects.nonNull(result)) {
          return REQUEST_APPROVED;
        }
      } catch (Exception e) {
        log.error(e.getMessage());
        e.printStackTrace();
      }
    }
    return REQUEST_APPROVAL_DENIED;
  }

  public String cancelRequest(int requestItemId, int employeeId) {
    Optional<Employee> employee = employeeRepository.findById(employeeId);
    if (employee.isPresent() && employee.get().getRoles().equals(EmployeeLevel.HOD.name())) {
      Optional<RequestItem> requestItem = findById(requestItemId);
      if (requestItem.isPresent() && requestItem.get().getStatus().equals(RequestStatus.PENDING)) {
        requestItem.get().setEndorsement(REJECTED);
        requestItem.get().setEndorsementDate(new Date());
        requestItem.get().setApproval(RequestApproval.REJECTED);
        requestItem.get().setApprovalDate(new Date());
        requestItem.get().setStatus(ENDORSEMENT_CANCELLED);
        RequestItem result = requestItemRepository.save(requestItem.get());
        if (Objects.nonNull(result)) {
          saveRequest(requestItemId, employeeId, ENDORSEMENT_CANCELLED);
          return REQUEST_CANCELLED;
        }
      }
    } else if (employee.isPresent()
        && employee.get().getRoles().equals(EmployeeLevel.GENERAL_MANAGER.name())) {
      Optional<RequestItem> requestItem = findById(requestItemId);
      if (requestItem.isPresent() && requestItem.get().getStatus().equals(RequestStatus.PENDING)) {
        requestItem.get().setEndorsement(REJECTED);
        requestItem.get().setEndorsementDate(new Date());
        requestItem.get().setApproval(RequestApproval.REJECTED);
        requestItem.get().setApprovalDate(new Date());
        requestItem.get().setStatus(APPROVAL_CANCELLED);
        RequestItem result = requestItemRepository.save(requestItem.get());
        if (Objects.nonNull(result)) {
          saveRequest(requestItemId, employeeId, APPROVAL_CANCELLED);
          return REQUEST_CANCELLED;
        }
      }
    }
    return REQUEST_PENDING;
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

  private void saveRequest(int requestItemId, int employeeId, RequestStatus status) {
    Request request = new Request();
    request.setRequestItemId(requestItemId);
    request.setStatus(status);
    request.setCreatedAt(new Date());
    request.setEmployeeId(employeeId);
    try {
      requestRepository.save(request);
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    }
    catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }
}
