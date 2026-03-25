# Java 21 + Virtual Threads Upgrade

**Date:** 2026-03-25
**Branch:** fix/grn-n-plus-one-query
**Status:** Approved

## Context

BlueSupply runs on Spring Boot 3.5.0 with JDK 17 and Java 17 source/target. The CI pipeline uses JDK 17. Virtual threads are not enabled. The production logs show blocking SMTP calls and bulk endorsement loops (~20 items each) tying up Tomcat request threads for several seconds per batch.

`jjwt` is already at `0.12.6` with the new API — no JWT work needed.

## Goals

1. Run the application on JDK 21 in CI and production.
2. Unlock Java 21 language features (pattern matching in switch, unnamed variables, sequenced collections).
3. Enable virtual threads so Tomcat request threads and `@Async` task executor threads become virtual, eliminating the thread-blocking cost of SMTP, JDBC, and PDF generation calls.

## Non-Goals

- Flyway 10 upgrade (deferred to Spring Boot 4 migration).
- OAuth2 mail authentication fix (separate infrastructure concern).
- Rewriting any business logic.

## Changes

### 1. CI workflow — `.github/workflows/maven.yml`

Change `java-version` from `17` to `21`. No other workflow changes needed; the `temurin` distribution supports 21.

### 2. `supply-svc/pom.xml`

- `<maven.compiler.source>17</maven.compiler.source>` → `21`
- `<maven.compiler.target>17</maven.compiler.target>` → `21`
- `maven-compiler-plugin` `<source>17</source>` / `<target>17</target>` → `21`

### 3. `supply-db/pom.xml`

- `<maven.compiler.source>17</maven.compiler.source>` → `21`
- `<maven.compiler.target>17</maven.compiler.target>` → `21`

### 4. `supply-svc/src/main/resources/application.properties`

Add:
```
spring.threads.virtual.enabled=true
```

This single property switches the Tomcat connector thread pool and Spring's default `AsyncTaskExecutor` to virtual threads. No code changes required.

## Effect

| Concern | Before | After |
|---|---|---|
| Tomcat request threads | Platform threads (OS-scheduled, ~1MB stack each) | Virtual threads (JVM-scheduled, ~few KB each) |
| `@Async` email/notification tasks | Platform threads from `taskExecutor` pool | Virtual threads |
| Bulk endorsement loops (20+ items, SMTP per batch) | Block request thread during SMTP connect (~8s) | Virtual thread parks; OS thread is freed |
| Java language level | 17 | 21 |

## Risk

Low. Spring Boot 3.2+ has full virtual thread support. All dependencies (Lombok 1.18.38, POI 5.4.0, Thymeleaf, Flying Saucer 9.13.0) are compatible with Java 21. No API breaks between Java 17 and 21 for the code in this repo.

The one known caveat: `synchronized` blocks pin virtual threads to their carrier OS thread. The codebase does not use explicit `synchronized` blocks in hot paths; Hibernate and JDBC drivers in use are known to work correctly with virtual threads.

## Files Changed

| File | Change |
|---|---|
| `.github/workflows/maven.yml` | `java-version: 17 → 21` |
| `supply-svc/pom.xml` | compiler source/target `17 → 21` (properties + plugin) |
| `supply-db/pom.xml` | compiler source/target `17 → 21` (properties) |
| `supply-svc/src/main/resources/application.properties` | add `spring.threads.virtual.enabled=true` |