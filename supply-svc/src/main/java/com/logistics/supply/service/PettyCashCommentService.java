package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.converter.PettyCashCommentConverter;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.model.PettyCashComment;
import com.logistics.supply.repository.PettyCashCommentRepository;
import com.logistics.supply.repository.PettyCashRepository;
import com.logistics.supply.util.CsvFileGenerator;
import com.logistics.supply.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.exception.PettyCashNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.logistics.supply.enums.RequestStatus.APPROVAL_CANCELLED;
import static com.logistics.supply.enums.RequestStatus.ENDORSEMENT_CANCELLED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PettyCashCommentService
    implements ICommentService<PettyCashComment, PettyCash.PettyCashMinorDTO> {
  private final PettyCashCommentConverter commentConverter;
  private final PettyCashCommentRepository pettyCashCommentRepository;
  private final PettyCashRepository pettyCashRepository;

  private PettyCashComment saveComment(PettyCashComment comment) {
    return pettyCashCommentRepository.save(comment);
  }

  @Transactional(rollbackFor = Exception.class)
  public PettyCashComment addComment(PettyCashComment pettyCashComment) {
    PettyCashComment saved = saveComment(pettyCashComment);
    setCommentStatusOnPettyCash(saved);
    return saved;
  }

  @Async
  public void setCommentStatusOnPettyCash(PettyCashComment saved) {
    PettyCash pettyCash1 =
        pettyCashRepository
            .findById(saved.getPettyCash().getId())
            .orElseThrow(() -> new PettyCashNotFoundException(saved.getPettyCash().getId()));
    Employee employee = saved.getEmployee();
    if (Helper.hasRole(employee, EmployeeRole.ROLE_HOD)
        || Helper.hasRole(employee, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      pettyCash1.setStatus(RequestStatus.COMMENT);
      pettyCashRepository.save(pettyCash1);
    }
  }

  @Override
  public List<CommentResponse<PettyCash.PettyCashMinorDTO>> findByCommentTypeId(int id) {
    List<PettyCashComment> pettyCashComments = pettyCashCommentRepository.findByPettyCashId(id);
    return commentConverter.convert(pettyCashComments);
  }

  @Override
  @Cacheable(value = "dataSheet", key = "#id")
  public ByteArrayInputStream getCommentDataSheet(int id) throws IOException {
    List<PettyCashComment> pettyCashComments = pettyCashCommentRepository.findByPettyCashId(id);
    List<List<String>> pcList =
        pettyCashComments.stream()
            .map(
                p ->
                    Arrays.asList(
                        String.valueOf(p.getId()),
                        p.getPettyCash().getPettyCashRef(),
                        p.getDescription(),
                        String.valueOf(p.getCreatedDate()),
                        p.getProcessWithComment().name(),
                        p.getEmployee().getFullName()))
            .collect(Collectors.toList());
    return CsvFileGenerator.toCSV(pcList);
  }

  private boolean hodNotRelatedToPettyCash(Employee employee, PettyCash pettyCash) {
    return employee.getRoles().stream()
            .anyMatch(f -> EmployeeRole.ROLE_HOD.name().equalsIgnoreCase(f.getName()))
        && employee.getDepartment() != pettyCash.getDepartment();
  }


  @Transactional(rollbackFor = Exception.class)
  public CommentResponse<PettyCash.PettyCashMinorDTO> savePettyCashComment(
          CommentDTO comment, int pettyCashId, Employee employee) {
    PettyCash pettyCash =
        pettyCashRepository
            .findById(pettyCashId)
            .orElseThrow(() -> new PettyCashNotFoundException(pettyCashId));

    PettyCashComment pettyCashComment =
        PettyCashComment.builder()
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .pettyCash(pettyCash)
            .employee(employee)
            .build();

    return commentConverter.convert(addComment(pettyCashComment));
  }

  public PettyCash cancelPettyCash(int pettyCashId, EmployeeRole employeeRole) {
    PettyCash pettyCash =
        pettyCashRepository
            .findById(pettyCashId)
            .orElseThrow(() -> new PettyCashNotFoundException(pettyCashId));
    if (employeeRole.equals(EmployeeRole.ROLE_GENERAL_MANAGER)) {
      pettyCash.setStatus(APPROVAL_CANCELLED);
      pettyCash.setDeleted(true);

    } else if (employeeRole.equals(EmployeeRole.ROLE_HOD)) {
      pettyCash.setStatus(ENDORSEMENT_CANCELLED);
      pettyCash.setDeleted(true);
    }
    return pettyCashRepository.save(pettyCash);
  }
}
