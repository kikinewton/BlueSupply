package com.logistics.supply.annotation.validator;

import com.logistics.supply.annotation.ValidApproved;
import com.logistics.supply.enums.RequestApproval;
import com.logistics.supply.model.RequestItem;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ApprovedValidator implements ConstraintValidator<ValidApproved, RequestItem> {

  public static boolean isApproved(RequestItem requestItem) {
    return requestItem.getApproval().equals(RequestApproval.APPROVED);
  }

  @Override
  public void initialize(ValidApproved constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(RequestItem requestItem, ConstraintValidatorContext constraintValidatorContext) {
    return isApproved(requestItem);
  }
}
