package com.logistics.supply.annotation;

import com.logistics.supply.annotation.validator.RequestItemValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = RequestItemValidator.class)
@Retention(RUNTIME)
@Target({FIELD, METHOD, PARAMETER})
public @interface ValidRequestItem {
  public String message() default "request item id not found ";

  public Class<?>[] groups() default {};

  public Class<? extends Payload>[] payload() default {};
}
