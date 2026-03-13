package com.logistics.supply.common.annotations;

import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@SqlGroup({
        @Sql(
                scripts = "classpath:/sql/init_script.sql",
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        ),
        @Sql(
                scripts = "classpath:/sql/cleanup_script.sql",
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
        )
})
public @interface ClearDbBeforeTestMethod {

    String CLEANUP_SCRIPT = "classpath:/sql/cleanup_script.sql";
    String INITIALISE_SCRIPT = "classpath:/sql/init_script.sql";

}
