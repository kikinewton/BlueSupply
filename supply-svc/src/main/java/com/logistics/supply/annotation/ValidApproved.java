package com.logistics.supply.annotation;


import com.logistics.supply.annotation.validator.ApprovedValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
    String message() default "Request item not approveD";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
