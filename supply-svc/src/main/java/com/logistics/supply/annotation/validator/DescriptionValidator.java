package com.logistics.supply.annotation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.logistics.supply.annotation.ValidDescription;

public class DescriptionValidator implements ConstraintValidator<ValidDescription, String> {

  public static boolean isValidDescription(String description) {
    description = description.trim();
    if (description.length() > 0) return !description.matches("[^a-zA-Z0-9]+");
    return false;
  }

  @Override
  public void initialize(ValidDescription constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
    return isValidDescription(s);
  }
}
