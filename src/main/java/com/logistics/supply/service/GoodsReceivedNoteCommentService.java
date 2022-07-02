package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.GrnMinorDTO;
import com.logistics.supply.dto.converter.GoodsReceivedNoteCommentConverter;
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
@Transactional
@RequiredArgsConstructor
public class GoodsReceivedNoteCommentService
    implements ICommentService<GoodsReceivedNoteComment, GrnMinorDTO> {
  private final GoodsReceivedNoteCommentConverter commentConverter;
  private final GoodsReceivedNoteCommentRepository goodsReceivedNoteCommentRepository;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;

  @SneakyThrows
  public GoodsReceivedNoteComment findByCommentId(long commentId) {
    return goodsReceivedNoteCommentRepository
        .findById(commentId)
        .orElseThrow(() -> new GeneralException(GRN_NOT_FOUND, HttpStatus.NOT_FOUND));
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public CommentResponse<GrnMinorDTO> saveGRNComment(
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
    return commentConverter.convert(addComment(grnComment));
  }

  @Override
  public GoodsReceivedNoteComment addComment(GoodsReceivedNoteComment comment) {
    return goodsReceivedNoteCommentRepository.save(comment);
  }

  @Override
  public List<CommentResponse<GrnMinorDTO>> findUnReadComment(int employeeId) {
    List<GoodsReceivedNoteComment> unReadComment =
        goodsReceivedNoteCommentRepository.findByReadFalseAndEmployeeId(employeeId);
    return commentConverter.convert(unReadComment);
  }

  @Override
  public List<CommentResponse<GrnMinorDTO>> findByCommentTypeId(int id) {
    List<GoodsReceivedNoteComment> goodsReceivedNotes = goodsReceivedNoteCommentRepository.findByGoodsReceivedNoteId(id);
    return commentConverter.convert(goodsReceivedNotes);
  }
}
