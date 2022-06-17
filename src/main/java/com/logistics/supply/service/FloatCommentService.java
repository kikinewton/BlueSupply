package com.logistics.supply.service;

import com.logistics.supply.dto.BulkCommentDTO;
import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.interfaces.ICommentService;
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

import java.util.List;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.FLOAT_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatCommentService implements ICommentService<FloatComment> {
  final FloatCommentRepository floatCommentRepository;
  final FloatOrderRepository floatOrderRepository;

  private FloatComment saveComment(FloatComment comment) {
    return floatCommentRepository.save(comment);
  }

  public FloatComment findByCommentId(long commentId) {
    return floatCommentRepository.findById(commentId).orElse(null);
  }

  @SneakyThrows
  public FloatComment addComment(FloatComment comment) {

    FloatComment saved = saveComment(comment);
    return floatOrderRepository
        .findById(saved.getFloats().getId())
        .map(
            x -> {
              x.setStatus(RequestStatus.COMMENT);
              FloatOrder f = floatOrderRepository.save(x);
              return saved;
            })
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @Override
  public List<FloatComment> findUnReadComment(int employeeId) {
    return null;
  }

  @Override
  public List<FloatComment> findByCommentTypeId(int id) {
    return floatCommentRepository.findByFloatsIdOrderByIdDesc(id);
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
            .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
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

  public List<FloatComment> saveFloatComments(
      BulkCommentDTO comments, Employee employee, EmployeeRole role) {
    List<FloatComment> floatComments =
        comments.getComments().stream()
            .map(
                c -> {
                  if (c.getCancelled() != null && c.getCancelled()) {
                    cancel(c.getProcurementTypeId(), role);
                  }
                  return saveFloatComment(c.getComment(), c.getProcurementTypeId(), employee);
                })
            .collect(Collectors.toList());
    return floatComments;
  }

  @SneakyThrows
  private FloatOrder cancel(int floatOrderId, EmployeeRole role) {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            order -> {
              switch (role) {
                case ROLE_HOD:
                  order.setEndorsement(EndorsementStatus.REJECTED);
                  order.setStatus(RequestStatus.ENDORSEMENT_CANCELLED);
                  break;
                default:
                  order.setApproval(RequestApproval.REJECTED);
                  order.setStatus(RequestStatus.APPROVAL_CANCELLED);
                  break;
              }
              return floatOrderRepository.save(order);
            })
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }
}
