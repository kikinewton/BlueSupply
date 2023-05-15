package com.logistics.supply.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.logistics.supply.annotation.validator.GMApprovedValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NotNull
@NotEmpty
@Documented
@Constraint(validatedBy = GMApprovedValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface ValidGMApproved {
    String message() default "REQUEST_ITEM_NOT_GM_APPROVED";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
