package com.logistics.supply.service;

import com.logistics.supply.dto.BulkFloatsDTO;
import com.logistics.supply.dto.FloatGrnDto;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.event.listener.GRNListener;
import com.logistics.supply.exception.FloatGrnNotFoundException;
import com.logistics.supply.exception.FloatOrderNotFoundException;
import com.logistics.supply.exception.NotFoundException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.model.FloatOrder;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatGRNRepository;
import com.logistics.supply.repository.FloatOrderRepository;
import com.logistics.supply.repository.FloatsRepository;
import com.logistics.supply.util.IdentifierUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

  public FloatGrnDto issueFloatGRN(BulkFloatsDTO bulkFloatsDTO, Employee employee) {

    Set<Floats> floats =
        bulkFloatsDTO.getFloats().stream()
            .map(f -> floatsRepository.findById(f.getId()).get())
            .collect(Collectors.toSet());
    Integer floatOrderId = floats.stream().findAny().get().getFloatOrder().getId();

      FloatGRN floatGRN = new FloatGRN();
      floatGRN.setFloats(floats);
      floatGRN.setFloatOrderId(floatOrderId);
      floatGRN.setCreatedBy(employee);
      String ref = IdentifierUtil.idHandler("FLG", "STORES", String.valueOf(count()));
      floatGRN.setFloatGrnRef(ref);
      floatGRN.setStatus(RequestApproval.PENDING);
      FloatGRN floatGRN1 = save(floatGRN);
      return FloatGrnDto.toDto(floatGRN1);

  }

  public FloatGRN findFloatGRN(Floats floats)  {
    return floatGRNRepository
        .findByFloats(floats)
        .orElseThrow(() -> new NotFoundException("Float GRN with float ref: %s not found".formatted(floats.getFloatRef())));
  }

  public FloatGRN findById(long floatGrnId)  {
    return floatGRNRepository
        .findById(floatGrnId)
        .orElseThrow(() -> new FloatGrnNotFoundException((int) floatGrnId));
  }

  @Transactional
  public FloatGrnDto approveByStoreManager(long floatGrnId, int approvedBy) {

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
    return FloatGrnDto.toDto(saved);
  }

  private void notifyAuditor(FloatGRN saved) {

    GRNListener.FloatGRNEvent floatGRNEvent = new GRNListener.FloatGRNEvent(this, saved);
    applicationEventPublisher.publishEvent(floatGRNEvent);
  }

  public List<FloatGrnDto> findFloatGrnPendingApproval(int departmentId) {
    List<FloatGrnDto> list = new ArrayList<>();
    for (FloatGRN floatGRN : floatGRNRepository.findPendingApproval(departmentId)) {
      FloatGrnDto f = FloatGrnDto.toDto(floatGRN);

      FloatOrder floatOrder = floatOrderRepository
              .findById(f.getFloatOrderId())
              .orElseThrow(
                      () ->
                              new FloatOrderNotFoundException(f.getFloatOrderId()));

      FloatOrder.FloatOrderDto floatOrderDTO = FloatOrder.FloatOrderDto.toDto(floatOrder);
      f.setFloatOrder(floatOrderDTO);
      list.add(f);
    }
    return list;
  }

  public List<FloatGRN> getAllApprovedFloatGRNForAuditor() {
    return floatGRNRepository.findByApprovedByStoreManagerIsTrueAndNotRetired();
  }

  public Page<FloatGrnDto> findAllFloatGrn(int departmentId, Pageable pageable) {
    return floatGRNRepository
        .findByCreatedByDepartmentId(departmentId, pageable)
        .map(FloatGrnDto::toDto)
        .map(
            f -> {
              FloatOrder floatOrder = null;
                floatOrder =
                    floatOrderRepository
                        .findById(f.getFloatOrderId())
                        .orElseThrow(
                            () ->
                                new FloatOrderNotFoundException(f.getFloatOrderId()));

              FloatOrder.FloatOrderDto floatOrderDTO = FloatOrder.FloatOrderDto.toDto(floatOrder);
              f.setFloatOrder(floatOrderDTO);
              return f;
            });
  }

  public long count() {
    return floatGRNRepository.count() + 1;
  }
}
