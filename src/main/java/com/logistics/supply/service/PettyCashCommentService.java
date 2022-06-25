package com.logistics.supply.service;

import com.logistics.supply.dto.BulkCommentDTO;
import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.PettyCashDTO;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.model.PettyCashComment;
import com.logistics.supply.repository.PettyCashCommentRepository;
import com.logistics.supply.repository.PettyCashRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.logistics.supply.enums.RequestStatus.APPROVAL_CANCELLED;
import static com.logistics.supply.enums.RequestStatus.ENDORSEMENT_CANCELLED;
import static com.logistics.supply.util.Constants.PETTY_CASH_NOT_FOUND;
import static com.logistics.supply.util.Helper.hasRole;

@Slf4j
@Service
@RequiredArgsConstructor
public class PettyCashCommentService implements ICommentService<PettyCashComment, PettyCashDTO> {

  final PettyCashCommentRepository pettyCashCommentRepository;
  final PettyCashRepository pettyCashRepository;

  private PettyCashComment saveComment(PettyCashComment comment) {
    return pettyCashCommentRepository.save(comment);
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public PettyCashComment addComment(PettyCashComment pettyCashComment) {
    PettyCashComment saved = saveComment(pettyCashComment);
    PettyCash pettyCash1 =
        pettyCashRepository
            .findById(saved.getPettyCash().getId())
            .orElseThrow(() -> new GeneralException(PETTY_CASH_NOT_FOUND, HttpStatus.NOT_FOUND));
    Employee employee = saved.getEmployee();
    if (hasRole(employee, EmployeeRole.ROLE_HOD)
        || hasRole(employee, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      pettyCash1.setStatus(RequestStatus.COMMENT);
      pettyCashRepository.save(pettyCash1);
    }
    return saved;
  }

  @Override
  public List<CommentResponse<PettyCashDTO>> findUnReadComment(int employeeId) {
    return null;
  }

  @Override
  public List<PettyCashComment> findByCommentTypeId(int id) {
    return pettyCashCommentRepository.findByPettyCashIdOrderByIdDesc(id);
  }

  private boolean hodNotRelatedToPettyCash(Employee employee, PettyCash pettyCash) {
    return employee.getRoles().stream()
            .anyMatch(f -> EmployeeRole.ROLE_HOD.name().equalsIgnoreCase(f.getName()))
        && employee.getDepartment() != pettyCash.getDepartment();
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public PettyCashComment savePettyCashComment(
      CommentDTO comment, int pettyCashId, Employee employee) {
    PettyCash pettyCash =
        pettyCashRepository
            .findById(pettyCashId)
            .orElseThrow(() -> new GeneralException("Petty cash not found", HttpStatus.NOT_FOUND));
    if (hodNotRelatedToPettyCash(employee, pettyCash))
      throw new GeneralException("Petty cash related to user", HttpStatus.NOT_FOUND);
    PettyCashComment pettyCashComment =
        PettyCashComment.builder()
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .pettyCash(pettyCash)
            .employee(employee)
            .build();

    return addComment(pettyCashComment);
  }

  public List<PettyCashComment> savePettyCashComments(
      BulkCommentDTO comments, Employee employee, EmployeeRole role) {
    List<PettyCashComment> pettyCashComments =
        comments.getComments().stream()
            .map(
                c -> {
                  if (c.getCancelled() != null && c.getCancelled()) {
                    cancelPettyCash(c.getProcurementTypeId(), role);
                  }
                  return savePettyCashComment(c.getComment(), c.getProcurementTypeId(), employee);
                })
            .collect(Collectors.toList());
    return pettyCashComments;
  }

  @SneakyThrows
  private PettyCash cancelPettyCash(int pettyCashId, EmployeeRole employeeRole) {
    return pettyCashRepository
        .findById(pettyCashId)
        .map(
            r -> {
              if (employeeRole.equals(EmployeeRole.ROLE_GENERAL_MANAGER)) {
                r.setStatus(APPROVAL_CANCELLED);
                return pettyCashRepository.save(r);
              } else if (employeeRole.equals(EmployeeRole.ROLE_HOD)) {
                r.setStatus(ENDORSEMENT_CANCELLED);
                return pettyCashRepository.save(r);
              }
              return null;
            })
        .orElseThrow(() -> new GeneralException(PETTY_CASH_NOT_FOUND, HttpStatus.NOT_FOUND));
  }
}
