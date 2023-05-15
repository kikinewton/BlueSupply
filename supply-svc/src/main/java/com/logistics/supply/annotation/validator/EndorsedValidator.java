package com.logistics.supply.annotation.validator;

import com.logistics.supply.annotation.ValidEndorsed;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.logistics.supply.model.RequestItem;

import static com.logistics.supply.enums.EndorsementStatus.ENDORSED;

public class EndorsedValidator implements ConstraintValidator<ValidEndorsed, RequestItem> {

  public static boolean isEndorsed(RequestItem requestItem) {
    return requestItem.getEndorsement().equals(ENDORSED);
  }

  @Override
  public void initialize(ValidEndorsed constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(RequestItem requestItem, ConstraintValidatorContext constraintValidatorContext) {
    return isEndorsed(requestItem);
  }
}
