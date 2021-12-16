package com.logistics.supply.annotation.validator;

import com.logistics.supply.annotation.ValidGMApproved;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.RequestItem;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GMApprovedValidator implements ConstraintValidator<ValidGMApproved, RequestItem> {

  public static boolean isGmApproved(RequestItem requestItem) {
    return requestItem.getApproval().equals(RequestApproval.APPROVED);
  }

  @Override
  public void initialize(ValidGMApproved constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(RequestItem requestItem, ConstraintValidatorContext constraintValidatorContext) {
    return isGmApproved(requestItem);
  }
}
