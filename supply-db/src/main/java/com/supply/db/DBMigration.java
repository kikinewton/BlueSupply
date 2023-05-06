package com.supply.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DBMigration implements CommandLineRunner {

    @Value("$db.service.url")
    private String url;
    @Value("$db.service.username")
    private String username;
    @Value("$db.service.password")
    private String password;


  public static void main(String[] args) {
      SpringApplication.run(DBMigration.class, args);
  }

    @Override
    public void run(String... args) throws Exception {
//        Flyway flyway
    }
}
