package com.logistics.supply.common.config;

import com.logistics.supply.db.DBMigration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class TestDbMigrationConfig {

    @MockBean
    private DBMigration dbMigration;
}
