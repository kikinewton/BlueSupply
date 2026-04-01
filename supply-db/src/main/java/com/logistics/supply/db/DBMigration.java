package com.logistics.supply.db;

import com.logistics.supply.db.service.PreMigrationBackup;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DBMigration implements CommandLineRunner {

    @Value("${db.service.url}")
    private String url;
    @Value("${db.service.username}")
    private String username;
    @Value("${db.service.password}")
    private String password;

    @Autowired
    private PreMigrationBackup preMigrationBackup;

  public static void main(String[] args) {
      SpringApplication.run(DBMigration.class, args);
  }

    @Override
    public void run(String... args) {
        preMigrationBackup.run();

        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();
        flyway.migrate();
    }
}
