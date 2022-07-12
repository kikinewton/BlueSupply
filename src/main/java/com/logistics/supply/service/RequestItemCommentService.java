package com.logistics.supply.service;

import com.logistics.supply.annotation.ValidRequestItem;
import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.RequestItemDTO;
import com.logistics.supply.dto.converter.RequestItemCommentConverter;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.RequestItemComment;
import com.logistics.supply.repository.RequestItemCommentRepository;
import com.logistics.supply.repository.RequestItemRepository;
import com.logistics.supply.util.CsvFileGenerator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.logistics.supply.enums.RequestProcess.HOD_REQUEST_ENDORSEMENT;
import static com.logistics.supply.enums.RequestStatus.APPROVAL_CANCELLED;
import static com.logistics.supply.enums.RequestStatus.ENDORSEMENT_CANCELLED;
import static com.logistics.supply.util.Constants.COMMENT_NOT_FOUND;
import static com.logistics.supply.util.Constants.REQUEST_ITEM_NOT_FOUND;
import static com.logistics.supply.util.Helper.hasRole;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestItemCommentService
    implements ICommentService<RequestItemComment, RequestItemDTO> {
  private final RequestItemCommentRepository requestItemCommentRepository;
  private final RequestItemCommentConverter commentConverter;
  private final RequestItemRepository requestItemRepository;

  private RequestItemComment saveComment(RequestItemComment comment) {
    return requestItemCommentRepository.save(comment);
  }

  @SneakyThrows
  public RequestItemComment findByCommentId(long commentId) {
    return requestItemCommentRepository
        .findById(commentId)
        .orElseThrow(() -> new GeneralException(COMMENT_NOT_FOUND, HttpStatus.BAD_REQUEST));
  }

  public boolean updateReadStatus(int commentId, ProcurementType procurementType) {
    return false;
  }

  @Override
  @Cacheable(value = "requestCommentById", key = "#id", unless = "#result.isEmpty == true")
  public List<CommentResponse<RequestItemDTO>> findByCommentTypeId(int id) {
    List<RequestItemComment> unReadComment = requestItemCommentRepository.findByRequestItemId(id);
    return commentConverter.convert(unReadComment);
  }

  @SneakyThrows
  //  @Caching(evict = {
  //          @CacheEvict(value = "requestCommentById", key = "#id")
  //  })
  @CacheEvict(value = "requestCommentById", allEntries = true)
  @Transactional(rollbackFor = Exception.class)
  public RequestItemComment addComment(RequestItemComment comment) {
    RequestItemComment saved = saveComment(comment);

    RequestItem requestItem =
        requestItemRepository
            .findById(saved.getRequestItem().getId())
            .orElseThrow(() -> new GeneralException(REQUEST_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));
    Employee commentBy = saved.getEmployee();
    if (hasRole(commentBy, EmployeeRole.ROLE_HOD)) {
      setHODCommentStatus(saved, requestItem);
      requestItemRepository.save(requestItem);
    }
    if (hasRole(commentBy, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      requestItem.setStatus(RequestStatus.COMMENT);
      requestItemRepository.save(requestItem);
    }
    return saved;
  }

  private void setHODCommentStatus(RequestItemComment saved, RequestItem x) {
    if (saved.getProcessWithComment().equals(HOD_REQUEST_ENDORSEMENT)) {
      x.setStatus(RequestStatus.COMMENT);
    } else x.setRequestReview(RequestReview.COMMENT);
  }

  @Transactional(rollbackFor = Exception.class)
  public CommentResponse<RequestItemDTO> saveRequestItemComment(
      CommentDTO comment, @ValidRequestItem int requestItemId, Employee employee) {
    RequestItem requestItem = requestItemRepository.findById(requestItemId).get();
    RequestItemComment requestItemComment =
        RequestItemComment.builder()
            .requestItem(requestItem)
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .employee(employee)
            .build();
    return commentConverter.convert(addComment(requestItemComment));
  }

  public RequestItem cancelRequestItem(int requestItemId, EmployeeRole employeeRole)
      throws GeneralException {
    RequestItem cancelItem =
        requestItemRepository
            .findById(requestItemId)
            .orElseThrow(() -> new GeneralException(REQUEST_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));
    switch (employeeRole) {
      case ROLE_GENERAL_MANAGER:
        cancelItem.setStatus(APPROVAL_CANCELLED);
        cancelItem.setDeleted(true);
        return requestItemRepository.save(cancelItem);
      case ROLE_HOD:
        cancelItem.setStatus(ENDORSEMENT_CANCELLED);
        cancelItem.setDeleted(true);
        return requestItemRepository.save(cancelItem);
    }
    throw new GeneralException("CANCEL REQUEST ITEM FAILED", HttpStatus.BAD_REQUEST);
  }

  @Override
  @Cacheable(value = "dataSheet", key = "#id")
  public ByteArrayInputStream getCommentDataSheet(int id) {
    List<RequestItemComment> requestItemComments =
        requestItemCommentRepository.findByRequestItemId(id);
    List<List<String>> ricList =
        requestItemComments.stream()
            .map(
                ric ->
                    Arrays.asList(
                        String.valueOf(ric.getId()),
                        ric.getRequestItem().getRequestItemRef(),
                        ric.getDescription(),
                        String.valueOf(ric.getCreatedDate()),
                        ric.getProcessWithComment().name(),
                        ric.getEmployee().getFullName()))
            .collect(Collectors.toList());
    return CsvFileGenerator.toCSV(ricList);
  }
}
