package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDto;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.EmployeeMinorDto;
import com.logistics.supply.dto.QuotationMinorDto;
import com.logistics.supply.exception.QuotationNotFoundException;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Quotation;
import com.logistics.supply.model.QuotationComment;
import com.logistics.supply.repository.QuotationCommentRepository;
import com.logistics.supply.repository.QuotationRepository;
import com.logistics.supply.util.CsvFileGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QuotationCommentService
        implements ICommentService<QuotationComment, QuotationMinorDto> {

    private final QuotationRepository quotationRepository;
    private final QuotationCommentRepository quotationCommentRepository;

    public CommentResponse<QuotationMinorDto> saveComment(
            CommentDto comment,
            int quotationId,
            Employee employee) {

        Quotation quotation =
                quotationRepository
                        .findById(quotationId)
                        .orElseThrow(() -> new QuotationNotFoundException(quotationId));

        QuotationComment quotationComment =
                QuotationComment.builder()
                        .quotation(quotation)
                        .description(comment.getDescription())
                        .processWithComment(comment.getProcess())
                        .employee(employee)
                        .build();
        QuotationComment saved = addComment(quotationComment);
        return CommentResponse.from(saved,
            EmployeeMinorDto.toDto(saved.getEmployee()),
            QuotationMinorDto.toDto(saved.getQuotation()));
    }

    @Override
    public QuotationComment addComment(QuotationComment comment) {

        return quotationCommentRepository.save(comment);
    }

    @Override
    public List<CommentResponse<QuotationMinorDto>> findByCommentTypeId(int id) {
        List<QuotationComment> unReadComment = quotationCommentRepository.findByQuotationId(id);
        return unReadComment.stream()
            .map(c -> CommentResponse.from(c,
                EmployeeMinorDto.toDto(c.getEmployee()),
                QuotationMinorDto.toDto(c.getQuotation())))
            .toList();
    }

    @Override
    @Cacheable(value = "dataSheet", key = "#id")
    public ByteArrayInputStream getCommentDataSheet(int id) throws IOException {

        List<QuotationComment> quotationComments = quotationCommentRepository.findByQuotationId(id);

        List<List<String>> qcList =
                quotationComments.stream()
                        .map(
                                qc ->
                                        Arrays.asList(
                                                String.valueOf(qc.getId()),
                                                qc.getQuotation().getQuotationRef(),
                                                qc.getDescription(),
                                                String.valueOf(qc.getCreatedDate()),
                                                qc.getProcessWithComment().name(),
                                                qc.getEmployee().getFullName()))
                        .collect(Collectors.toList());

        return CsvFileGenerator.toCSV(qcList);
    }
}
