package com.logistics.supply.service;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.RequestItemComment;
import com.logistics.supply.repository.RequestItemCommentRepository;
import com.logistics.supply.repository.RequestItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    try {
      return requestItemCommentRepository.save(comment);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public RequestItemComment findByCommentId(long commentId) {
    return requestItemCommentRepository.findById(commentId).orElse(null);
  }

  public List<RequestItemComment> findByRequestItemId(int requestItemId) {
    try {
      return requestItemCommentRepository.findByRequestItemIdOrderByIdDesc(requestItemId);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ArrayList<>();
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
                    if(Objects.nonNull(r)) return saved;
                  } else if (saved
                      .getEmployee()
                      .getRoles()
                      .get(0)
                      .getName()
                      .equalsIgnoreCase(EmployeeRole.ROLE_GENERAL_MANAGER.name())) {
                    x.setApproval(RequestApproval.COMMENT);
                    RequestItem r = requestItemRepository.save(x);
                    if(Objects.nonNull(r)) return saved;
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
      x.setEndorsement(EndorsementStatus.COMMENT);
    } else x.setRequestReview(RequestReview.COMMENT);
  }
}
