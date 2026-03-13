package com.logistics.supply.common.annotations;

import com.logistics.supply.SupplyApplication;
import com.logistics.supply.common.config.TestAsyncConfig;
import com.logistics.supply.common.config.TestDbMigrationConfig;
import com.logistics.supply.common.config.TestEmailSenderConfig;
import com.logistics.supply.common.containers.PostgresqlTestContainer;
import com.logistics.supply.common.extension.AsyncCompletionExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SupplyApplication.class)
@ExtendWith(AsyncCompletionExtension.class)
@ContextConfiguration(initializers = {PostgresqlTestContainer.Initializer.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ClearDbBeforeTestMethod
@Import({TestEmailSenderConfig.class, TestDbMigrationConfig.class, TestAsyncConfig.class})
public @interface IntegrationTest {
}
