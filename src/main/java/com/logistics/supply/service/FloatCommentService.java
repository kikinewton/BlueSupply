package com.logistics.supply.service;

import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.model.FloatComment;
import com.logistics.supply.model.FloatOrder;
import com.logistics.supply.repository.FloatCommentRepository;
import com.logistics.supply.repository.FloatOrderRepository;
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
}
