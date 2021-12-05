package com.logistics.supply.service;

import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.enums.RequestReview;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.GoodsReceivedNoteComment;
import com.logistics.supply.model.RequestItem;
import com.logistics.supply.model.RequestItemComment;
import com.logistics.supply.repository.GoodsReceivedNoteCommentRepository;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
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
      return goodsReceivedNoteCommentRepository.findByGoodsReceivedNoteIdOrderByIdDesc(goodsReceivedNoteId);
    } catch (Exception e) {
      log.error(e.getMessage());
    }
    return new ArrayList<>();
  }



}
