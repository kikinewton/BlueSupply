package com.logistics.supply.service;

import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.CommentResponse;
import com.logistics.supply.dto.converter.PettyCashCommentConverter;
import com.logistics.supply.enums.RequestStatus;
import com.logistics.supply.errorhandling.GeneralException;
import com.logistics.supply.interfaces.ICommentService;
import com.logistics.supply.model.Employee;
import com.logistics.supply.model.EmployeeRole;
import com.logistics.supply.model.PettyCash;
import com.logistics.supply.model.PettyCashComment;
import com.logistics.supply.repository.PettyCashCommentRepository;
import com.logistics.supply.repository.PettyCashRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.logistics.supply.enums.RequestStatus.APPROVAL_CANCELLED;
import static com.logistics.supply.enums.RequestStatus.ENDORSEMENT_CANCELLED;
import static com.logistics.supply.util.Constants.PETTY_CASH_NOT_FOUND;
import static com.logistics.supply.util.Helper.hasRole;

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

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public PettyCashComment addComment(PettyCashComment pettyCashComment) {
    PettyCashComment saved = saveComment(pettyCashComment);
    PettyCash pettyCash1 =
        pettyCashRepository
            .findById(saved.getPettyCash().getId())
            .orElseThrow(() -> new GeneralException(PETTY_CASH_NOT_FOUND, HttpStatus.NOT_FOUND));
    Employee employee = saved.getEmployee();
    if (hasRole(employee, EmployeeRole.ROLE_HOD)
        || hasRole(employee, EmployeeRole.ROLE_GENERAL_MANAGER)) {
      pettyCash1.setStatus(RequestStatus.COMMENT);
      pettyCashRepository.save(pettyCash1);
    }
    return saved;
  }

  @Override
  public List<CommentResponse<PettyCash.PettyCashMinorDTO>> findUnReadComment(int employeeId) {
    return null;
  }

  @Override
  public List<CommentResponse<PettyCash.PettyCashMinorDTO>> findByCommentTypeId(int id) {
    List<PettyCashComment> pettyCashComments = pettyCashCommentRepository.findByPettyCashId(id);
    return commentConverter.convert(pettyCashComments);
  }

  private boolean hodNotRelatedToPettyCash(Employee employee, PettyCash pettyCash) {
    return employee.getRoles().stream()
            .anyMatch(f -> EmployeeRole.ROLE_HOD.name().equalsIgnoreCase(f.getName()))
        && employee.getDepartment() != pettyCash.getDepartment();
  }

  @SneakyThrows
  @Transactional(rollbackFor = Exception.class)
  public CommentResponse<PettyCash.PettyCashMinorDTO> savePettyCashComment(
      CommentDTO comment, int pettyCashId, Employee employee) {
    PettyCash pettyCash =
        pettyCashRepository
            .findById(pettyCashId)
            .orElseThrow(() -> new GeneralException(PETTY_CASH_NOT_FOUND, HttpStatus.NOT_FOUND));
    if (hodNotRelatedToPettyCash(employee, pettyCash))
      throw new GeneralException("PETTY CASH NOT RELATED TO USER", HttpStatus.NOT_FOUND);
    PettyCashComment pettyCashComment =
        PettyCashComment.builder()
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .pettyCash(pettyCash)
            .employee(employee)
            .build();

    return commentConverter.convert(addComment(pettyCashComment));
  }

  public PettyCash cancelPettyCash(int pettyCashId, EmployeeRole employeeRole)
      throws GeneralException {
    PettyCash pettyCash =
        pettyCashRepository
            .findById(pettyCashId)
            .orElseThrow(() -> new GeneralException(PETTY_CASH_NOT_FOUND, HttpStatus.NOT_FOUND));
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
