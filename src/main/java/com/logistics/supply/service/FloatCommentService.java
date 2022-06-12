package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.FloatComment;
import com.logistics.supply.model.FloatOrder;
import com.logistics.supply.repository.FloatCommentRepository;
import com.logistics.supply.repository.FloatOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatCommentService {
  final FloatCommentRepository floatCommentRepository;
  final FloatOrderRepository floatOrderRepository;

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

        return floatOrderRepository
            .findById(saved.getFloats().getId())
            .map(
                x -> {
                  x.setStatus(RequestStatus.COMMENT);
                  FloatOrder f = floatOrderRepository.save(x);
                  if (Objects.nonNull(f)) return saved;
                  return null;
                })
            .orElse(null);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  private boolean hodNotRelatedToFloats(Employee employee, FloatOrder floats) {
    return employee.getRoles().stream()
            .anyMatch(r -> EmployeeRole.ROLE_HOD.name().equalsIgnoreCase(r.getName()))
        && employee.getDepartment() != floats.getDepartment();
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public FloatComment saveFloatComment(CommentDTO comment, int floatId, Employee employee) {
    FloatOrder floats =
        floatOrderRepository
            .findById(floatId)
            .orElseThrow(() -> new GeneralException("Float not found", HttpStatus.NOT_FOUND));
    if (hodNotRelatedToFloats(employee, floats))
      throw new GeneralException("Float not related to department", HttpStatus.NOT_FOUND);

    FloatComment floatComment =
        FloatComment.builder()
            .floats(floats)
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .employee(employee)
            .build();

    return addComment(floatComment);
  }
}
