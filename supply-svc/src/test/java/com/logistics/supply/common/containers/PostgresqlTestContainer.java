package com.logistics.supply.common.containers;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresqlTestContainer extends PostgreSQLContainer<PostgresqlTestContainer> {

    private static final String IMAGE_VERSION = "postgres:12.9";
    private static PostgresqlTestContainer container;

    private PostgresqlTestContainer() {
        super(IMAGE_VERSION);
    }

    public static PostgresqlTestContainer getInstance() {
        if (container == null) {
            container = new PostgresqlTestContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            PostgresqlTestContainer container = PostgresqlTestContainer.getInstance();
            container.start();
            TestPropertyValues.of(
                    "datasource.postgres.read.jdbc-url=%s".formatted(container.getJdbcUrl()),
                    "datasource.postgres.read.username=%s".formatted(container.getUsername()),
                    "datasource.postgres.read.password=%s".formatted(container.getPassword()),
                    "datasource.postgres.write.jdbc-url=%s".formatted(container.getJdbcUrl()),
                    "datasource.postgres.write.username=%s".formatted(container.getUsername()),
                    "datasource.postgres.write.password=%s".formatted(container.getPassword()),
                    "app.scheduling.enable=false",
                    "app.data-setup.enable=false"
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
