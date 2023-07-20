package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDto;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.GrnMinorDto;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.GoodsReceivedNote;
import com.logistics.supply.model.GoodsReceivedNoteComment;
import com.logistics.supply.repository.GoodsReceivedNoteCommentRepository;
import com.logistics.supply.repository.GoodsReceivedNoteRepository;
import com.logistics.supply.util.CsvFileGenerator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.supply.dto.converter.GoodsReceivedNoteCommentConverter;
import com.logistics.supply.exception.CommentNotFoundException;
import com.logistics.supply.exception.GrnNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GoodsReceivedNoteCommentService
    implements ICommentService<GoodsReceivedNoteComment, GrnMinorDto> {
  private final GoodsReceivedNoteCommentConverter commentConverter;
  private final GoodsReceivedNoteCommentRepository goodsReceivedNoteCommentRepository;
  private final GoodsReceivedNoteRepository goodsReceivedNoteRepository;

  @SneakyThrows
  public GoodsReceivedNoteComment findByCommentId(long commentId) {
    return goodsReceivedNoteCommentRepository
        .findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("GRN (comment id)".formatted(commentId)));
  }

  @Transactional(rollbackFor = Exception.class)
  public CommentResponse<GrnMinorDto> saveGRNComment(
          CommentDto comment, long grnId, Employee employee) {
    GoodsReceivedNote goodsReceivedNote =
        goodsReceivedNoteRepository
            .findById(grnId)
            .orElseThrow(() -> new GrnNotFoundException((int) grnId));
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
  public List<CommentResponse<GrnMinorDto>> findByCommentTypeId(int id) {
    List<GoodsReceivedNoteComment> goodsReceivedNotes =
        goodsReceivedNoteCommentRepository.findByGoodsReceivedNoteId(id);
    return commentConverter.convert(goodsReceivedNotes);
  }

  @Override
  @Cacheable(value = "dataSheet", key = "#id")
  public ByteArrayInputStream getCommentDataSheet(int id) throws IOException {
    List<GoodsReceivedNoteComment> grnComments =
        goodsReceivedNoteCommentRepository.findByGoodsReceivedNoteId(id);
    List<List<String>> grnList =
        grnComments.stream()
            .map(
                g ->
                    Arrays.asList(
                        String.valueOf(g.getId()),
                        g.getGoodsReceivedNote().getGrnRef(),
                        g.getDescription(),
                        String.valueOf(g.getCreatedDate()),
                        g.getProcessWithComment().name(),
                        g.getEmployee().getFullName()))
            .collect(Collectors.toList());
    return CsvFileGenerator.toCSV(grnList);
  }
}
