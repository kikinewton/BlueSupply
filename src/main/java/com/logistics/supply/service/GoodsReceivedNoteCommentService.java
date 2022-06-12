package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.errorhandling.GeneralException;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsReceivedNoteCommentService {

  final GoodsReceivedNoteCommentRepository goodsReceivedNoteCommentRepository;
  final GoodsReceivedNoteRepository goodsReceivedNoteRepository;

  public GoodsReceivedNoteComment saveComment(GoodsReceivedNoteComment comment) {
    try {
      return goodsReceivedNoteCommentRepository.save(comment);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return null;
  }

  public GoodsReceivedNoteComment findByCommentId(long commentId) {
    return goodsReceivedNoteCommentRepository.findById(commentId).orElse(null);
  }

  public List<GoodsReceivedNoteComment> findByGoodsReceivedNoteId(long goodsReceivedNoteId) {
    try {
      return goodsReceivedNoteCommentRepository.findByGoodsReceivedNoteIdOrderByIdDesc(
          goodsReceivedNoteId);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ArrayList<>();
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public GoodsReceivedNoteComment saveGRNComment(
      CommentDTO comment, long grnId, Employee employee) {
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteRepository
            .findById(grnId)
            .orElseThrow(() -> new GeneralException("GRN not found", HttpStatus.NOT_FOUND));
    GoodsReceivedNoteComment grnComment =
        GoodsReceivedNoteComment.builder()
            .goodsReceivedNote(goodsReceivedNote)
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .employee(employee)
            .build();
    return saveComment(grnComment);
  }
}
