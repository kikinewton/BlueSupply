package com.logistics.supply.util;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.exception.RequestItemEndorsementException;
import com.logistics.supply.exception.RequestItemStatusException;
import com.logistics.supply.exception.RequestItemUpdateDetailsException;
import com.logistics.supply.model.RequestItem;

import static com.logistics.supply.enums.EndorsementStatus.ENDORSED;
import static com.logistics.supply.enums.RequestStatus.PROCESSED;

public class RequestItemValidatorUtil {

  RequestItemValidatorUtil() {}

  public static void validateRequestItemIsNotEndorsed(RequestItem requestItem) {
    if (requestItem.getEndorsement() == EndorsementStatus.PENDING
        && requestItem.getSuppliedBy() == null) {
      return;
    }
    throw new RequestItemEndorsementException(
        "Request item with id: %s failed validation for endorsement".formatted(requestItem));
  }

  public static void validateRequestItemIsNotProcessed(RequestItem requestItem) {

    if (requestItem.getEndorsement() == ENDORSED
        && requestItem.getStatus() != PROCESSED
        && requestItem.getSuppliedBy() == null) {
      return;
    }
    throw new RequestItemStatusException(
        "Request item with id: %s failed validation for process status"
            .formatted(requestItem.getId()));
  }

  public static void validateRequestItemIsNotApproved(RequestItem requestItem) {
    if (requestItem.getEndorsement() != ENDORSED) {
      throw new RequestItemStatusException(
          "Request item %s must be ENDORSED before approval (current: %s)"
              .formatted(requestItem.getId(), requestItem.getEndorsement()));
    }
    if (requestItem.getStatus() != PROCESSED) {
      throw new RequestItemStatusException(
          "Request item %s must be PROCESSED before approval (current: %s)"
              .formatted(requestItem.getId(), requestItem.getStatus()));
    }
    if (requestItem.getApproval() == RequestApproval.APPROVED) {
      throw new RequestItemStatusException(
          "Request item %s is already approved".formatted(requestItem.getId()));
    }
  }

  public static void validateRequestItemCanBeUpdated(
      RequestItem requestItem, String modifiedByEmail) {
    if (requestItem.getEmployee().getEmail().equalsIgnoreCase(modifiedByEmail)) {
      return;
    }
    throw new RequestItemUpdateDetailsException(requestItem.getId());
  }

  public static void isGmApproved(RequestItem requestItem) {
    boolean approved = requestItem.getApproval().equals(RequestApproval.APPROVED);
    if (approved) {
      return;
    }
    throw new RequestItemStatusException("Request item with id %s not approved by GM");
  }
}
