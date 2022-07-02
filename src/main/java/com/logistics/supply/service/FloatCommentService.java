package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.converter.FloatCommentConverter;
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

import static com.logistics.supply.util.Constants.COMMENT_NOT_FOUND;
import static com.logistics.supply.util.Constants.FLOAT_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class FloatCommentService implements ICommentService<FloatComment, FloatOrder.FloatOrderDTO> {
  private final FloatCommentRepository floatCommentRepository;
  private final FloatOrderRepository floatOrderRepository;

  private final FloatCommentConverter commentConverter;

  private FloatComment saveComment(FloatComment comment) {
    return floatCommentRepository.save(comment);
  }

  public FloatComment findByCommentId(long commentId) throws GeneralException {
    return floatCommentRepository
        .findById(commentId)
        .orElseThrow(() -> new GeneralException(COMMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
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
  public List<CommentResponse<FloatOrder.FloatOrderDTO>> findUnReadComment(int employeeId) {
    return null;
  }


  @Override
  public List<CommentResponse<FloatOrder.FloatOrderDTO>> findByCommentTypeId(int id) {
    List<FloatComment> comments = floatCommentRepository.findByFloats_IdEquals(id);
    return commentConverter.convert(comments);
  }

  private boolean hodNotRelatedToFloats(Employee employee, FloatOrder floats) {
    return employee.getRoles().stream()
            .anyMatch(r -> EmployeeRole.ROLE_HOD.name().equalsIgnoreCase(r.getName()))
        && employee.getDepartment() != floats.getDepartment();
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public CommentResponse<FloatOrder.FloatOrderDTO> saveFloatComment(CommentDTO comment, int floatId, Employee employee) {
    FloatOrder floats =
        floatOrderRepository
            .findById(floatId)
            .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
    if (hodNotRelatedToFloats(employee, floats))
      throw new GeneralException("FLOAT NOT RELATED TO DEPARTMENT", HttpStatus.NOT_FOUND);

    FloatComment floatComment =
        FloatComment.builder()
            .floats(floats)
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .employee(employee)
            .build();

    return commentConverter.convert(addComment(floatComment));
  }


  public FloatOrder cancel(int floatOrderId, EmployeeRole role) throws GeneralException {
    return floatOrderRepository
        .findById(floatOrderId)
        .map(
            order -> {
              switch (role) {
                case ROLE_HOD:
                  order.setEndorsement(EndorsementStatus.REJECTED);
                  order.setStatus(RequestStatus.ENDORSEMENT_CANCELLED);
                  order.setDeleted(true);
                  break;
                default:
                  order.setApproval(RequestApproval.REJECTED);
                  order.setStatus(RequestStatus.APPROVAL_CANCELLED);
                  order.setDeleted(true);
                  break;
              }
              return floatOrderRepository.save(order);
            })
        .orElseThrow(() -> new GeneralException(FLOAT_NOT_FOUND, HttpStatus.NOT_FOUND));
  }
}
