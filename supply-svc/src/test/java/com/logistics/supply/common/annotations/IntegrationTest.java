package com.logistics.supply.common.annotations;

import com.logistics.supply.SupplyApplication;
import com.logistics.supply.common.containers.PostgresqlTestContainer;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SupplyApplication.class)
@ContextConfiguration(initializers = {PostgresqlTestContainer.Initializer.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Commit
@ClearDbBeforeTestMethod
public @interface IntegrationTest {
}
