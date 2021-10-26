package com.logistics.supply.annotation.validator;

import com.logistics.supply.annotation.ValidRequestItem;
import com.logistics.supply.repository.RequestItemRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RequestItemValidator implements ConstraintValidator<ValidRequestItem, Integer> {

  @Autowired RequestItemRepository requestItemRepository;

  @Override
  public void initialize(ValidRequestItem constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }

  @Override
  public boolean isValid(
      Integer requestItemId, ConstraintValidatorContext constraintValidatorContext) {
    return requestItemRepository.existsById(requestItemId);
  }
}
