package com.logistics.supply.annotation;

import com.logistics.supply.annotation.validator.DescriptionValidator;
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
@Constraint(validatedBy = DescriptionValidator.class)
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface ValidDescription {

    String message() default "Invalid description";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
