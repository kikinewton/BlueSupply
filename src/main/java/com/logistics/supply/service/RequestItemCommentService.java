package com.logistics.supply.service;

import com.logistics.supply.annotation.ValidRequestItem;
import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.RequestItemComment;
import com.logistics.supply.repository.RequestItemCommentRepository;
import com.logistics.supply.repository.RequestItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.logistics.supply.enums.RequestProcess.HOD_REQUEST_ENDORSEMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestItemCommentService {

  final RequestItemCommentRepository requestItemCommentRepository;
  final RequestItemRepository requestItemRepository;

  private RequestItemComment saveComment(RequestItemComment comment) {
      return requestItemCommentRepository.save(comment);
  }

  @SneakyThrows
  public RequestItemComment findByCommentId(long commentId)  {
    return requestItemCommentRepository.findById(commentId).orElseThrow(() ->new GeneralException("Comment not found", HttpStatus.BAD_REQUEST));
  }

  public boolean updateReadStatus(int commentId, ProcurementType procurementType) {
    return false;
  }

  public List<RequestItemComment> findUnReadComment(int employeeId) {
    return requestItemCommentRepository.findUnReadEmployeeComment(employeeId);
  }

  public List<RequestItemComment> findByRequestItemId(int requestItemId) {
      return requestItemCommentRepository.findByRequestItemIdOrderByIdDesc(requestItemId);
  }

  public RequestItemComment addComment(RequestItemComment comment) {
    try {
      RequestItemComment saved = saveComment(comment);
      if (Objects.nonNull(saved)) {

        return requestItemRepository
            .findById(saved.getRequestItem().getId())
            .map(
                x -> {
                  if (saved
                      .getEmployee()
                      .getRoles()
                      .get(0)
                      .getName()
                      .equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())) {
                    setHODCommentStatus(saved, x);
                    RequestItem r = requestItemRepository.save(x);
                    if (Objects.nonNull(r)) return saved;
                  } else if (saved
                      .getEmployee()
                      .getRoles()
                      .get(0)
                      .getName()
                      .equalsIgnoreCase(EmployeeRole.ROLE_GENERAL_MANAGER.name())) {
                    x.setStatus(RequestStatus.COMMENT);
                    RequestItem r = requestItemRepository.save(x);
                    if (Objects.nonNull(r)) return saved;
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

  private void setHODCommentStatus(RequestItemComment saved, RequestItem x) {
    if (saved.getProcessWithComment().equals(HOD_REQUEST_ENDORSEMENT)) {
      x.setStatus(RequestStatus.COMMENT);
    } else x.setRequestReview(RequestReview.COMMENT);
  }

  @Transactional(rollbackFor = Exception.class)
  public RequestItemComment saveRequestItemComment(
          CommentDTO comment, @ValidRequestItem int requestItemId, Employee employee) {
    RequestItem requestItem = requestItemRepository.findById(requestItemId).get();
    RequestItemComment requestItemComment =
            RequestItemComment.builder()
                    .requestItem(requestItem)
                    .processWithComment(comment.getProcess())
                    .description(comment.getDescription())
                    .employee(employee)
                    .build();

    try {
      return addComment(requestItemComment);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }
}
