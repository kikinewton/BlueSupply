package com.logistics.supply.service;

import com.logistics.supply.dto.FloatGrnDto;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.FloatGrnComment;
import com.logistics.supply.util.CsvFileGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.converter.FloatGRNCommentConverter;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.exception.FloatGrnNotFoundException;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.FloatGRN;
import com.logistics.supply.repository.FloatGRNCommentRepository;
import com.logistics.supply.repository.FloatGRNRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FloatGRNCommentService implements ICommentService<FloatGrnComment, FloatGrnDto> {

  private final FloatGRNCommentRepository floatGRNCommentRepository;
  private final FloatGRNCommentConverter floatGRNCommentConverter;
  private final FloatGRNRepository floatGRNRepository;

  private FloatGrnComment saveComment(FloatGrnComment comment) {
    return floatGRNCommentRepository.save(comment);
  }

  @Override
  public FloatGrnComment addComment(FloatGrnComment comment) {
    FloatGrnComment savedComment = saveComment(comment);
    return floatGRNRepository
        .findById(savedComment.getFloatGRN().getId())
        .map(
            f -> {
              f.setStatus(RequestApproval.COMMENT);
              FloatGRN floatGRN = floatGRNRepository.save(f);
              return savedComment;
            })
        .orElseThrow(() -> new FloatGrnNotFoundException((int) savedComment.getFloatGRN().getId()));
  }

  @Override
  public List<CommentResponse<FloatGrnDto>> findByCommentTypeId(int id) {
    List<FloatGrnComment> comments = floatGRNCommentRepository.findByFloatGRNId(id);
    return floatGRNCommentConverter.convert(comments);
  }

  @Transactional
  public CommentResponse<FloatGrnDto> saveFloatGRNComment(
      CommentDTO comment, long floatGrnId, Employee employee) {
    FloatGRN floatGRN =
        floatGRNRepository
            .findById(floatGrnId)
            .orElseThrow(() -> new FloatGrnNotFoundException((int) floatGrnId));
    FloatGrnComment floatGRNComment =
        FloatGrnComment.builder()
            .floatGRN(floatGRN)
            .employee(employee)
            .description(comment.getDescription())
            .processWithComment(comment.getProcess())
            .build();
    return floatGRNCommentConverter.convert(addComment(floatGRNComment));
  }

  @Override
  public ByteArrayInputStream getCommentDataSheet(int id) throws IOException {
    List<FloatGrnComment> comments = floatGRNCommentRepository.findByFloatGRNId(id);
    List<List<String>> result =
        comments.stream()
            .map(
                c ->
                    Arrays.asList(
                        String.valueOf(c.getId()),
                        c.getFloatGRN().getFloatGrnRef(),
                        c.getDescription(),
                        String.valueOf(c.getCreatedDate()),
                        c.getProcessWithComment().name(),
                        c.getEmployee().getFullName()))
            .collect(Collectors.toList());
    return CsvFileGenerator.toCSV(result);
  }
}
