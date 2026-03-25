# Java 21 + Virtual Threads Upgrade

**Date:** 2026-03-25
**Branch:** fix/grn-n-plus-one-query
**Status:** Approved

## Context

BlueSupply runs on Spring Boot 3.5.0 with JDK 17 and Java 17 source/target. The CI pipeline uses JDK 17. Virtual threads are not enabled. The production logs show blocking SMTP calls and bulk endorsement loops (~20 items each) tying up Tomcat request threads for several seconds per batch.

`jjwt` is already at `0.12.6` with the new API — no JWT work needed.

A custom `AsyncConfig` class (`AsyncConfigurer` with four `ThreadPoolTaskExecutor` beans) exists. Because it overrides `getAsyncExecutor()`, Spring Boot's virtual thread auto-configuration does **not** reach `@Async` paths. `AsyncConfig` must be updated alongside the property change to get the full benefit.

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

Note: `supply-db/pom.xml` also has a `maven-compiler-plugin` block (lines 50–53) that contains no explicit `<source>`/`<target>` tags — it inherits from the `<properties>` block above, so no additional plugin change is needed there.

### 4. `supply-svc/src/main/resources/application.properties`

Add:
```
spring.threads.virtual.enabled=true
```

This switches the Tomcat connector thread pool to virtual threads. For `@Async` paths, see change 5.

The `application.async.core-pool-size`, `application.async.max-pool-size`, and `application.async.queue-capacity` properties are retained in `application.properties` for backward compatibility but become inert after `AsyncConfig` is updated (virtual threads do not need a bounded pool).

### 5. `AsyncConfig.java` — swap `ThreadPoolTaskExecutor` for `SimpleAsyncTaskExecutor`

`AsyncConfig` implements `AsyncConfigurer` and defines four named `ThreadPoolTaskExecutor` beans (`taskExecutor`, `repositoryTaskExecutor`, `serviceTaskExecutor`, `controllerTaskExecutor`). Because it overrides `getAsyncExecutor()`, Spring Boot's virtual thread executor does not apply to `@Async` methods.

Replace the `newTaskExecutor` factory method to use `SimpleAsyncTaskExecutor` with virtual threads:

```java
private Executor newTaskExecutor(final String taskExecutorNamePrefix) {
    SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor(taskExecutorNamePrefix);
    executor.setVirtualThreads(true);
    return executor;
}
```

`SimpleAsyncTaskExecutor.setVirtualThreads(true)` is available since Spring 6.1 (included in Spring Boot 3.2+). It creates a new virtual thread per submitted task — no pool needed. The four named executor beans retain their names so existing `@Async` annotations continue to work without changes. The only named `@Async` call in the codebase is `@Async(AsyncConfig.TASK_EXECUTOR_SERVICE)` in `FileGenerationUtil` — it will continue to resolve to the `serviceTaskExecutor` bean.

While updating `AsyncConfig`, also fix a pre-existing copy-paste bug: `TASK_EXECUTOR_NAME_PREFIX_REPOSITORY` is incorrectly set to `"serviceTaskExecutor-"` (same as the service prefix). Correct it to `"repositoryTaskExecutor-"` so thread names reflect the right executor.

Remove the now-unused imports and fields: `ThreadPoolTaskExecutor`, the three `@Value` pool-size fields, and the `@Value` import. **Do not remove `@Profile("!test")`** — that annotation intentionally excludes `AsyncConfig` from the test context (tests use a different executor setup). Keeping it prevents `AsyncConfig`'s virtual-thread executors from interfering with test-context task execution.

## Effect

| Concern | Before | After |
|---|---|---|
| Tomcat request threads | Platform threads (~1 MB stack each) | Virtual threads (few KB each) |
| `@Async` email/notification tasks | Platform threads from bounded `ThreadPoolTaskExecutor` | Virtual threads via `SimpleAsyncTaskExecutor` |
| Bulk endorsement loops (20+ items, SMTP per batch) | Block request thread during SMTP connect (~8 s) | Virtual thread parks; carrier OS thread is freed |
| Java language level | 17 | 21 |

## Risk

Low. Spring Boot 3.2+ has full virtual thread support. All dependencies (Lombok 1.18.38, POI 5.4.0, Thymeleaf, Flying Saucer 9.13.0) are compatible with Java 21. No API breaks between Java 17 and 21 for the code in this repo.

The one known caveat: `synchronized` blocks pin virtual threads to their carrier OS thread. The codebase does not use explicit `synchronized` blocks in hot paths. Hibernate and the PostgreSQL JDBC driver in use are known to work correctly with virtual threads.

## Files Changed

| File | Change |
|---|---|
| `.github/workflows/maven.yml` | `java-version: 17 → 21` |
| `supply-svc/pom.xml` | compiler source/target `17 → 21` (properties + plugin) |
| `supply-db/pom.xml` | compiler source/target `17 → 21` (properties only) |
| `supply-svc/src/main/resources/application.properties` | add `spring.threads.virtual.enabled=true` |
| `AsyncConfig.java` | replace `ThreadPoolTaskExecutor` with `SimpleAsyncTaskExecutor(virtualThreads=true)` |
