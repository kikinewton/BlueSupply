package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
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
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PettyCashCommentService {

  final PettyCashCommentRepository pettyCashCommentRepository;
  final PettyCashRepository pettyCashRepository;

  private PettyCashComment saveComment(PettyCashComment comment) {
    return pettyCashCommentRepository.save(comment);
  }

  public PettyCashComment findByCommentId(long commentId) {
    return pettyCashCommentRepository.findById(commentId).orElse(null);
  }

  public List<PettyCashComment> findByPettyCashId(int pettyCashId) {
    return pettyCashCommentRepository.findByPettyCashIdOrderByIdDesc(pettyCashId);
  }

  public PettyCashComment addComment(PettyCashComment pettyCashComment) {
    try {
      PettyCashComment saved = saveComment(pettyCashComment);
      if (Objects.nonNull(saved)) {
        return pettyCashRepository
            .findById(saved.getPettyCash().getId())
            .map(
                x -> {
                  if (saved
                      .getEmployee()
                      .getRoles()
                      .get(0)
                      .getName()
                      .equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())) {
                    x.setStatus(RequestStatus.COMMENT);
                    PettyCash pettyCash = pettyCashRepository.save(x);
                    if (Objects.nonNull(pettyCash)) return saved;
                  } else if (saved
                      .getEmployee()
                      .getRoles()
                      .get(0)
                      .getName()
                      .equalsIgnoreCase(EmployeeRole.ROLE_GENERAL_MANAGER.name())) {
                    x.setStatus(RequestStatus.COMMENT);
                    PettyCash pettyCash = pettyCashRepository.save(x);
                    if (Objects.nonNull(pettyCash)) return saved;
                  }
                  return null;
                })
            .orElse(null);
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
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
}
