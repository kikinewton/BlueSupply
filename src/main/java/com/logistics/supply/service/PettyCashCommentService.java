package com.logistics.supply.service;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.model.PettyCashComment;
import com.logistics.supply.repository.PettyCashCommentRepository;
import com.logistics.supply.repository.PettyCashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PettyCashCommentService {

  final PettyCashCommentRepository pettyCashCommentRepository;
  final PettyCashRepository pettyCashRepository;

  private PettyCashComment saveComment(PettyCashComment comment) {
    try {
      return pettyCashCommentRepository.save(comment);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  public PettyCashComment findByCommentId(long commentId) {
    return pettyCashCommentRepository.findById(commentId).orElse(null);
  }

  public List<PettyCashComment> findByPettyCashId(int pettyCashId) {
    try {
      return pettyCashCommentRepository.findByPettyCashIdOrderByIdDesc(pettyCashId);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return new ArrayList<>();
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
                    x.setEndorsement(EndorsementStatus.COMMENT);
                    PettyCash pettyCash = pettyCashRepository.save(x);
                    if (Objects.nonNull(pettyCash)) return saved;
                  } else if (saved
                      .getEmployee()
                      .getRoles()
                      .get(0)
                      .getName()
                      .equalsIgnoreCase(EmployeeRole.ROLE_GENERAL_MANAGER.name())) {
                    x.setApproval(RequestApproval.COMMENT);
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
}
