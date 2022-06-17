package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.GoodsReceivedNoteComment;
import com.logistics.supply.repository.GoodsReceivedNoteCommentRepository;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.logistics.supply.util.Constants.GRN_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsReceivedNoteCommentService implements ICommentService<GoodsReceivedNoteComment> {

  final GoodsReceivedNoteCommentRepository goodsReceivedNoteCommentRepository;
  final GoodsReceivedNoteRepository goodsReceivedNoteRepository;

  @SneakyThrows
  public GoodsReceivedNoteComment findByCommentId(long commentId) {
    return goodsReceivedNoteCommentRepository
        .findById(commentId)
        .orElseThrow(() -> new GeneralException(GRN_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNoteComment saveGRNComment(
      CommentDTO comment, long grnId, Employee employee) {
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteRepository
            .findById(grnId)
            .orElseThrow(() -> new GeneralException(GRN_NOT_FOUND, HttpStatus.NOT_FOUND));
    GoodsReceivedNoteComment grnComment =
        GoodsReceivedNoteComment.builder()
            .goodsReceivedNote(goodsReceivedNote)
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .employee(employee)
            .build();
    return addComment(grnComment);
  }

  @Override
  public GoodsReceivedNoteComment addComment(GoodsReceivedNoteComment comment) {
    return goodsReceivedNoteCommentRepository.save(comment);
  }

  @Override
  public List<GoodsReceivedNoteComment> findUnReadComment(int employeeId) {
    return null;
  }

  @Override
  public List<GoodsReceivedNoteComment> findByCommentTypeId(int id) {
    return goodsReceivedNoteCommentRepository.findByGoodsReceivedNoteIdOrderByIdDesc(id);
  }
}
