package com.logistics.supply.service;

import com.logistics.supply.dto.BulkFloatsDTO;
import com.logistics.supply.dto.FloatGrnDTO;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.event.listener.GRNListener;
import com.logistics.supply.exception.FloatOrderNotFoundException;
import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatGRNRepository;
import com.logistics.supply.repository.FloatOrderRepository;
import com.logistics.supply.repository.FloatsRepository;
import com.logistics.supply.util.Constants;
import com.logistics.supply.util.IdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.supply.exception.FloatGrnNotFoundException;
import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.model.FloatOrder;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatGRNService {
  private final FloatGRNRepository floatGRNRepository;
  private final FloatsRepository floatsRepository;
  private final FloatOrderRepository floatOrderRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  public FloatGRN save(FloatGRN floatGRN) {
    return floatGRNRepository.save(floatGRN);
  }

  public FloatGrnDTO issueFloatGRN(BulkFloatsDTO bulkFloatsDTO, Employee employee)
      throws GeneralException {
    Set<Floats> floats =
        bulkFloatsDTO.getFloats().stream()
            .map(f -> floatsRepository.findById(f.getId()).get())
            .collect(Collectors.toSet());
    Integer floatOrderId = floats.stream().findAny().get().getFloatOrder().getId();
    try {
      FloatGRN floatGRN = new FloatGRN();
      floatGRN.setFloats(floats);
      floatGRN.setFloatOrderId(floatOrderId);
      floatGRN.setCreatedBy(employee);
      String ref = IdentifierUtil.idHandler("FLG", "STORES", String.valueOf(count()));
      floatGRN.setFloatGrnRef(ref);
      floatGRN.setStatus(RequestApproval.PENDING);
      FloatGRN floatGRN1 = save(floatGRN);
      return FloatGrnDTO.toDto(floatGRN1);
    } catch (Exception e) {
      log.error(e.toString());
    }
    throw new GeneralException(Constants.FLOAT_GRN_FAILED, HttpStatus.BAD_REQUEST);
  }

  public FloatGRN findFloatGRN(Floats floats) throws GeneralException {
    return floatGRNRepository
        .findByFloats(floats)
        .orElseThrow(() -> new NotFoundException("Float GRN with float ref: %s not found".formatted(floats.getFloatRef())));
  }

  public FloatGRN findById(long floatGrnId) throws GeneralException {
    return floatGRNRepository
        .findById(floatGrnId)
        .orElseThrow(() -> new FloatGrnNotFoundException((int) floatGrnId));
  }

  @Transactional
  public FloatGrnDTO approveByStoreManager(long floatGrnId, int approvedBy)
      throws GeneralException {
    FloatGRN floatGRN =
        floatGRNRepository
            .findById(floatGrnId)
            .orElseThrow(() -> new FloatGrnNotFoundException((int) floatGrnId));
    floatGRN.setApprovedByStoreManager(true);
    floatGRN.setDateOfApprovalByStoreManager(new Date());
    floatGRN.setEmployeeStoreManager(approvedBy);
    floatGRN.setStatus(RequestApproval.APPROVED);
    FloatGRN saved = floatGRNRepository.save(floatGRN);
    notifyAuditor(saved);
    return FloatGrnDTO.toDto(saved);
  }

  private void notifyAuditor(FloatGRN saved) {
    GRNListener.FloatGRNEvent floatGRNEvent = new GRNListener.FloatGRNEvent(this, saved);
    applicationEventPublisher.publishEvent(floatGRNEvent);
  }

  public List<FloatGrnDTO> findFloatGrnPendingApproval(int departmentId) {
    return floatGRNRepository.findPendingApproval(departmentId).stream()
        .map(FloatGrnDTO::toDto)
        .map(
            f -> {
              FloatOrder floatOrder = null;

                floatOrder =
                    floatOrderRepository
                        .findById(f.getFloatOrderId())
                        .orElseThrow(
                            () ->
                                new FloatOrderNotFoundException(f.getFloatOrderId()));

              FloatOrder.FloatOrderDTO floatOrderDTO = FloatOrder.FloatOrderDTO.toDto(floatOrder);
              f.setFloatOrder(floatOrderDTO);
              return f;
            })
        .collect(Collectors.toList());
  }

  public List<FloatGRN> getAllApprovedFloatGRNForAuditor() {
    return floatGRNRepository.findByApprovedByStoreManagerIsTrueAndNotRetired();
  }

  public Page<FloatGrnDTO> findAllFloatGrn(int departmentId, Pageable pageable) {
    return floatGRNRepository
        .findByCreatedByDepartmentId(departmentId, pageable)
        .map(FloatGrnDTO::toDto)
        .map(
            f -> {
              FloatOrder floatOrder = null;
                floatOrder =
                    floatOrderRepository
                        .findById(f.getFloatOrderId())
                        .orElseThrow(
                            () ->
                                new FloatOrderNotFoundException(f.getFloatOrderId()));

              FloatOrder.FloatOrderDTO floatOrderDTO = FloatOrder.FloatOrderDTO.toDto(floatOrder);
              f.setFloatOrder(floatOrderDTO);
              return f;
            });
  }

  public long count() {
    return floatGRNRepository.count() + 1;
  }
}
