package com.logistics.supply.common.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.jdbc.Sql;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public @interface ClearDbBeforeTestMethod {

    String CLEANUP_SCRIPT = "classpath:/sql/cleanup_script.sql";
    String INITIALISE_SCRIPT = "classpath:/sql/init_script.sql";

    @AliasFor(annotation = Sql.class, attribute = "scripts")
    String[] db() default {
            CLEANUP_SCRIPT,
            INITIALISE_SCRIPT
    };


}
