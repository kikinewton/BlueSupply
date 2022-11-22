package com.logistics.supply.annotation;

import com.logistics.supply.annotation.validator.ApprovedValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NotNull
@NotEmpty
@Documented
@Constraint(validatedBy = ApprovedValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface ValidApproved {
    String message() default "REQUEST ITEM NOT APPROVED";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
