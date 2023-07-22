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
  String message() default "Request item not found ";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
