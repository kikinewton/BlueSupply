package com.logistics.supply.controller;

import com.logistics.supply.dto.BulkCommentDTO;
import com.logistics.supply.dto.CommentDTO;
import com.logistics.supply.dto.ResponseDTO;
import com.logistics.supply.enums.ProcurementType;
import com.logistics.supply.model.*;
import com.logistics.supply.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.logistics.supply.util.Constants.SUCCESS;
import static com.logistics.supply.util.Helper.failedResponse;
import static com.logistics.supply.util.Helper.notFound;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

  final RequestItemCommentService requestItemCommentService;
  final FloatCommentService floatCommentService;
  final FloatService floatService;
  final FloatOrderService floatOrderService;
  final GoodsReceivedNoteCommentService goodsReceivedNoteCommentService;
  final GoodsReceivedNoteService goodsReceivedNoteService;
  final PettyCashCommentService pettyCashCommentService;
  final PettyCashService pettyCashService;
  final RequestItemService requestItemService;
  final EmployeeService employeeService;
  final RoleService roleService;

  @PostMapping("/comment/{procurementType}")
  @PreAuthorize("hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_HOD')")
  public ResponseEntity<?> addRequestComment(
      @Valid @RequestBody BulkCommentDTO comments,
      @Valid @PathVariable ProcurementType procurementType,
      Authentication authentication) {
    try {
      Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
      EmployeeRole role = roleService.getEmployeeRole(authentication);

      switch (procurementType) {
        case LPO:
          List<RequestItemComment> commentResult =
              comments.getComments().stream()
                  .map(
                      c -> {
                        if (c.getCancelled() != null && c.getCancelled() == true) {
                          requestItemService.cancelRequestItem(c.getProcurementTypeId(), role);
                        }
                        return saveRequestItemComment(
                            c.getComment(), c.getProcurementTypeId(), employee);
                      })
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());

          if (commentResult.isEmpty()) return failedResponse("COMMENT_NOT_SAVED");
          ResponseDTO response = new ResponseDTO("COMMENTS_SAVED", SUCCESS, commentResult);
          return ResponseEntity.ok(response);
        case FLOAT:
          List<FloatComment> floatComments =
              comments.getComments().stream()
                  .map(
                      c -> {
                        if (c.getCancelled() != null && c.getCancelled()) {
                          floatOrderService.cancel(c.getProcurementTypeId(), role);
                        }
                        return saveFloatComment(c.getComment(), c.getProcurementTypeId(), employee);
                      })
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
          if (floatComments.isEmpty()) return failedResponse("COMMENT_NOT_SAVED");
          ResponseDTO responseFloat = new ResponseDTO("COMMENTS_SAVED", SUCCESS, floatComments);
          return ResponseEntity.ok(responseFloat);
        case PETTY_CASH:
          List<PettyCashComment> pettyCashComments =
              comments.getComments().stream()
                  .map(
                      c -> {
                        if (c.getCancelled() != null && c.getCancelled()) {
                          pettyCashService.cancelPettyCash(c.getProcurementTypeId(), role);
                        }
                        return savePettyCashComment(
                            c.getComment(), c.getProcurementTypeId(), employee);
                      })
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());

          if (pettyCashComments.isEmpty()) return failedResponse("COMMENTS_NOT_SAVED");
          ResponseDTO responsePettyComment =
              new ResponseDTO("COMMENT_SAVED", SUCCESS, pettyCashComments);
          return ResponseEntity.ok(responsePettyComment);
      }

    } catch (Exception e) {
      log.error(e.toString());
    }
    return failedResponse("ADD_COMMENT_FAILED");
  }

  @PostMapping("/comment/goodsReceivedNote/{goodsReceivedNoteId}")
  @PreAuthorize(
      "hasRole('ROLE_GENERAL_MANAGER') or hasRole('ROLE_HOD') or hasRole('ROLE_PROCUREMENT_MANAGER')")
  public ResponseEntity<?> postGRNComment(
      @PathVariable("goodsReceivedNoteId") int goodsReceivedNoteId,
      @RequestBody BulkCommentDTO comments,
      Authentication authentication) {
    Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
    List<GoodsReceivedNoteComment> savedComments =
        comments.getComments().stream()
            .map(c -> saveGRNComment(c.getComment(), goodsReceivedNoteId, employee))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (savedComments.isEmpty()) return failedResponse("ADD_COMMENT_FAILED");
    ResponseDTO responseGRNComment = new ResponseDTO("COMMENT_SAVED", SUCCESS, savedComments);
    return ResponseEntity.ok(responseGRNComment);
  }

  @GetMapping(value = "/comment/unread")
  public ResponseEntity<?> findUnReadComment(
      @RequestParam ProcurementType procurementType, Authentication authentication) {
    try {
      switch (procurementType) {
        case LPO:
          Employee employee = employeeService.findEmployeeByEmail(authentication.getName());
          List<RequestItemComment> comments =
              requestItemCommentService.findUnReadComment(employee.getId());
          ResponseDTO responseLpoComment =
              new ResponseDTO("FETCH_UNREAD_LPO_COMMENT", SUCCESS, comments);
          return ResponseEntity.ok(responseLpoComment);
      }
    } catch (Exception e) {
      log.error(e.toString());
    }
    return notFound("NO_COMMENT_FOUND");
  }

  private RequestItemComment saveRequestItemComment(
      CommentDTO comment, int requestItemId, Employee employee) {
    Optional<RequestItem> requestItem = requestItemService.findById(requestItemId);
    if (!requestItem.isPresent()) return null;
    //    if (hodNotRelatedToRequestItem(employee, requestItem)) return null;
    RequestItemComment requestItemComment =
        RequestItemComment.builder()
            .requestItem(requestItem.get())
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .employee(employee)
            .build();

    try {
      return requestItemCommentService.addComment(requestItemComment);
    } catch (Exception e) {
      log.error(e.toString());
    }
    return null;
  }

  private boolean hodNotRelatedToRequestItem(Employee employee, Optional<RequestItem> requestItem) {
    return employee.getRoles().get(0).getName().equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())
        && employee.getDepartment() == requestItem.get().getUserDepartment();
  }

  private boolean hodNotRelatedToFloats(Employee employee, Floats floats) {
    return employee.getRoles().get(0).getName().equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())
        && employee.getDepartment() != floats.getDepartment();
  }

  private boolean hodNotRelatedToPettyCash(Employee employee, PettyCash pettyCash) {
    return employee.getRoles().get(0).getName().equalsIgnoreCase(EmployeeRole.ROLE_HOD.name())
        && employee.getDepartment() != pettyCash.getDepartment();
  }

  private FloatComment saveFloatComment(CommentDTO comment, int floatId, Employee employee) {
    Floats floats = floatService.findById(floatId);
    if (Objects.isNull(floats)) return null;
    if (hodNotRelatedToFloats(employee, floats)) return null;

    FloatComment floatComment =
        FloatComment.builder()
            .floats(floats)
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .employee(employee)
            .build();

    return floatCommentService.addComment(floatComment);
  }

  private PettyCashComment savePettyCashComment(
      CommentDTO comment, int pettyCashId, Employee employee) {
    PettyCash pettyCash = pettyCashService.findById(pettyCashId);
    if (Objects.isNull(pettyCash)) return null;
    if (hodNotRelatedToPettyCash(employee, pettyCash)) return null;
    PettyCashComment pettyCashComment =
        PettyCashComment.builder()
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .pettyCash(pettyCash)
            .employee(employee)
            .build();

    return pettyCashCommentService.addComment(pettyCashComment);
  }

  private GoodsReceivedNoteComment saveGRNComment(
      CommentDTO comment, long grnId, Employee employee) {
    GoodsReceivedNote goodsReceivedNote = goodsReceivedNoteService.findGRNById(grnId);
    if (Objects.isNull(goodsReceivedNote)) return null;
    GoodsReceivedNoteComment grnComment =
        GoodsReceivedNoteComment.builder()
            .goodsReceivedNote(goodsReceivedNote)
            .processWithComment(comment.getProcess())
            .description(comment.getDescription())
            .employee(employee)
            .build();
    return goodsReceivedNoteCommentService.saveComment(grnComment);
  }
}
