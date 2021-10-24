package com.logistics.supply.service;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.FloatComment;
import com.logistics.supply.model.Floats;
import com.logistics.supply.repository.FloatCommentRepository;
import com.logistics.supply.repository.FloatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatCommentService {
  final FloatCommentRepository floatCommentRepository;
  final FloatsRepository floatsRepository;

  private FloatComment saveComment(FloatComment comment) {
    try {
      return floatCommentRepository.save(comment);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public FloatComment findByCommentId(long commentId) {
    return floatCommentRepository.findById(commentId).orElse(null);
  }

  public List<FloatComment> findByRequestItemId(int requestItemId) {
    try {
      return floatCommentRepository.findByFloatsIdOrderByIdDesc(requestItemId);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ArrayList<>();
  }

  public FloatComment addComment(FloatComment comment) {
    try {
      FloatComment saved = saveComment(comment);
      if (Objects.nonNull(saved)) {

        return floatsRepository
            .findById(saved.getFloats().getId())
            .map(
                x -> {
                  if (saved
                      .getEmployee()
                      .getRoles()
                      .get(0)
                      .getName()
                      .equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())) {
                    x.setEndorsement(EndorsementStatus.COMMENT);
                    Floats f = floatsRepository.save(x);
                    if (Objects.nonNull(f)) return saved;
                  } else if (saved
                      .getEmployee()
                      .getRoles()
                      .get(0)
                      .getName()
                      .equalsIgnoreCase(EmployeeRole.ROLE_GENERAL_MANAGER.name())) {
                    x.setApproval(RequestApproval.COMMENT);
                    Floats f = floatsRepository.save(x);
                    if (Objects.nonNull(f)) return saved;
                  }
                  return null;
                })
            .orElse(null);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }
}
