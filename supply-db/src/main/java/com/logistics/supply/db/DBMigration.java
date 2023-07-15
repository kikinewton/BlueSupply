package com.logistics.supply.db;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DBMigration implements CommandLineRunner {

    @Value("${db.service.url}")
    private String url;
    @Value("${db.service.username}")
    private String username;
    @Value("${db.service.password}")
    private String password;

  public static void main(String[] args) {
      SpringApplication.run(DBMigration.class, args);
  }

    @Override
    public void run(String... args) {
        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();
        flyway.migrate();
    }
}
