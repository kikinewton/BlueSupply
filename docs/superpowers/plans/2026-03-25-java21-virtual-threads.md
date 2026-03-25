# Java 21 + Virtual Threads Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bump the build and CI to Java 21 and replace the `ThreadPoolTaskExecutor` async configuration with virtual-thread-backed executors, eliminating platform-thread blocking on SMTP, JDBC, and PDF paths.

**Architecture:** Five targeted changes — two pom.xml compiler bumps, one CI java-version bump, one application property, and one config class rewrite. No business logic changes. `AsyncConfig` is excluded from the test context via `@Profile("!test")`, so `TestAsyncConfig` (which uses `SyncTaskExecutor`) is unaffected and requires no changes.

**Tech Stack:** Spring Boot 3.5.0, Spring Framework 6.1+, JDK 21 Temurin, `SimpleAsyncTaskExecutor.setVirtualThreads(true)`, Maven Surefire, GitHub Actions

---

## File Map

| File | Action | What changes |
|---|---|---|
| `.github/workflows/maven.yml` | Modify | `java-version: 17 → 21` |
| `supply-svc/pom.xml` | Modify | `maven.compiler.source/target 17 → 21` in `<properties>` and `maven-compiler-plugin` |
| `supply-db/pom.xml` | Modify | `maven.compiler.source/target 17 → 21` in `<properties>` only |
| `supply-svc/src/main/resources/application.properties` | Modify | Add `spring.threads.virtual.enabled=true` |
| `supply-svc/src/main/java/com/logistics/supply/configuration/AsyncConfig.java` | Modify | Replace `ThreadPoolTaskExecutor` with `SimpleAsyncTaskExecutor(virtualThreads=true)`; fix `TASK_EXECUTOR_NAME_PREFIX_REPOSITORY` copy-paste bug; remove `@Value` pool-size fields |
| `supply-svc/src/test/java/com/logistics/supply/configuration/AsyncConfigTest.java` | Create | Unit test: executor produces virtual threads; repository thread prefix is distinct from service prefix *(addition beyond spec's 5 files — test coverage for Task 2)* |

---

## Task 1: Bump Java 21 in pom files and CI

**Files:**
- Modify: `.github/workflows/maven.yml:22`
- Modify: `supply-svc/pom.xml:18-19` (properties) and `supply-svc/pom.xml:273-274` (compiler plugin)
- Modify: `supply-db/pom.xml:18-19` (properties)

- [ ] **Step 1: Update CI workflow**

In `.github/workflows/maven.yml`, change line 22:
```yaml
        java-version: '21'
```

Full updated step block for reference:
```yaml
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: temurin
        cache: maven
```

- [ ] **Step 2: Update supply-svc compiler properties**

In `supply-svc/pom.xml`, in `<properties>` (lines 18–19):
```xml
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
```

- [ ] **Step 3: Update supply-svc compiler plugin**

In `supply-svc/pom.xml`, inside the `maven-compiler-plugin` `<configuration>` block (lines 273–274):
```xml
                    <source>21</source>
                    <target>21</target>
```

- [ ] **Step 4: Update supply-db compiler properties**

In `supply-db/pom.xml`, in `<properties>` (lines 18–19):
```xml
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
```

Note: `supply-db/pom.xml` has a `maven-compiler-plugin` block with **no** `<source>`/`<target>` tags inside — it inherits from properties, so no plugin change is needed there.

- [ ] **Step 5: Verify the build compiles on JDK 21**

Prerequisite: your local JDK must be 21. Check with `java -version`.

```bash
./mvnw clean package -Dmaven.test.skip=true
```

Expected: `BUILD SUCCESS`. If you see `unsupported release: 21` you are running JDK 17 locally — install JDK 21 and set `JAVA_HOME` before continuing.

- [ ] **Step 6: Commit**

```bash
git add .github/workflows/maven.yml supply-svc/pom.xml supply-db/pom.xml
git commit -m "build: bump Java compiler and CI to 21"
```

---

## Task 2: TDD — migrate AsyncConfig to virtual threads

**Files:**
- Create: `supply-svc/src/test/java/com/logistics/supply/configuration/AsyncConfigTest.java`
- Modify: `supply-svc/src/main/java/com/logistics/supply/configuration/AsyncConfig.java`

### Background

`AsyncConfig` implements `AsyncConfigurer` and is annotated `@Profile("!test")`, meaning it is **excluded from the test Spring context**. Because of this, we test it as a plain unit test (no `@SpringBootTest`) — simply instantiate `new AsyncConfig()` and call its public methods directly.

Before the change, `getAsyncExecutor()` returns a `ThreadPoolTaskExecutor` that is never Spring-initialized (because we're not in a Spring context). Calling `execute()` on it throws `IllegalStateException`. The test will fail on that exception — which is the expected red state.

After the change, `AsyncConfig` has no `@Value` fields, so `new AsyncConfig()` works correctly in isolation.

- [ ] **Step 1: Write the failing test**

Create `supply-svc/src/test/java/com/logistics/supply/configuration/AsyncConfigTest.java`:

```java
package com.logistics.supply.configuration;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {

    @Test
    void getAsyncExecutor_submitsToVirtualThread() throws InterruptedException {
        AsyncConfig config = new AsyncConfig();
        Executor executor = config.getAsyncExecutor();

        AtomicBoolean isVirtual = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        executor.execute(() -> {
            isVirtual.set(Thread.currentThread().isVirtual());
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Task did not complete within 5 seconds");
        assertTrue(isVirtual.get(), "Expected a virtual thread but task ran on a platform thread");
    }

    @Test
    void repositoryExecutor_threadNameIsDistinctFromServiceExecutor() throws InterruptedException {
        AsyncConfig config = new AsyncConfig();
        Executor repoExecutor = config.getRepositoryAsyncExecutor();
        Executor serviceExecutor = config.getServiceAsyncExecutor();

        AtomicReference<String> repoThreadName = new AtomicReference<>();
        AtomicReference<String> serviceThreadName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(2);

        repoExecutor.execute(() -> {
            repoThreadName.set(Thread.currentThread().getName());
            latch.countDown();
        });
        serviceExecutor.execute(() -> {
            serviceThreadName.set(Thread.currentThread().getName());
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertNotNull(repoThreadName.get());
        assertNotNull(serviceThreadName.get());
        assertFalse(
            repoThreadName.get().startsWith("serviceTaskExecutor-"),
            "Repository executor thread name should not start with 'serviceTaskExecutor-' " +
            "(copy-paste bug). Got: " + repoThreadName.get()
        );
        assertTrue(
            repoThreadName.get().startsWith("repositoryTaskExecutor-"),
            "Repository executor thread name should start with 'repositoryTaskExecutor-'. " +
            "Got: " + repoThreadName.get()
        );
    }
}
```

- [ ] **Step 2: Run the tests to confirm they fail (red)**

```bash
./mvnw test -pl supply-svc -Dtest=AsyncConfigTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected: Both tests FAIL with `IllegalStateException` — calling `execute()` on an uninitialized `ThreadPoolTaskExecutor` (no Spring context = no `afterPropertiesSet()`) throws before any assertion runs. That is the expected red state.

Note on `repositoryExecutor_threadNameIsDistinctFromServiceExecutor`: its primary purpose is catching the copy-paste bug (`TASK_EXECUTOR_NAME_PREFIX_REPOSITORY = "serviceTaskExecutor-"`). That assertion only fires once the executor is actually functional — i.e., after the fix in Step 3. In the current red state it fails for the same uninitialized-executor reason as the first test.

- [ ] **Step 3: Rewrite `AsyncConfig` to use `SimpleAsyncTaskExecutor`**

Replace the entire contents of `AsyncConfig.java` with:

```java
package com.logistics.supply.configuration;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Profile("!test")
public class AsyncConfig implements AsyncConfigurer {

    public static final String TASK_EXECUTOR_REPOSITORY = "repositoryTaskExecutor";
    public static final String TASK_EXECUTOR_SERVICE = "serviceTaskExecutor";
    public static final String TASK_EXECUTOR_CONTROLLER = "controllerTaskExecutor";
    private static final String TASK_EXECUTOR_DEFAULT = "taskExecutor";
    private static final String TASK_EXECUTOR_NAME_PREFIX_DEFAULT = "taskExecutor-";
    private static final String TASK_EXECUTOR_NAME_PREFIX_REPOSITORY = "repositoryTaskExecutor-";
    private static final String TASK_EXECUTOR_NAME_PREFIX_CONTROLLER = "controllerTaskExecutor-";
    private static final String TASK_EXECUTOR_NAME_PREFIX_SERVICE = "serviceTaskExecutor-";

    @Override
    @Bean(name = TASK_EXECUTOR_DEFAULT)
    public Executor getAsyncExecutor() {
        return newTaskExecutor(TASK_EXECUTOR_NAME_PREFIX_DEFAULT);
    }

    @Bean(name = TASK_EXECUTOR_REPOSITORY)
    public Executor getRepositoryAsyncExecutor() {
        return newTaskExecutor(TASK_EXECUTOR_NAME_PREFIX_REPOSITORY);
    }

    @Bean(name = TASK_EXECUTOR_SERVICE)
    public Executor getServiceAsyncExecutor() {
        return newTaskExecutor(TASK_EXECUTOR_NAME_PREFIX_SERVICE);
    }

    @Bean(name = TASK_EXECUTOR_CONTROLLER)
    public Executor getControllerAsyncExecutor() {
        return newTaskExecutor(TASK_EXECUTOR_NAME_PREFIX_CONTROLLER);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    private Executor newTaskExecutor(final String taskExecutorNamePrefix) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor(taskExecutorNamePrefix);
        executor.setVirtualThreads(true);
        return executor;
    }
}
```

Key changes from the original:
- `ThreadPoolTaskExecutor` → `SimpleAsyncTaskExecutor` with `setVirtualThreads(true)`
- Removed `@Value` fields (`CORE_POOL_SIZE`, `MAX_POOL_SIZE`, `QUEUE_CAPACITY`)
- Fixed copy-paste bug: `TASK_EXECUTOR_NAME_PREFIX_REPOSITORY` was `"serviceTaskExecutor-"`, now correctly `"repositoryTaskExecutor-"`
- Removed `@Autowired`, `@Value`, `ThreadPoolTaskExecutor` imports

- [ ] **Step 4: Run the tests to confirm they pass (green)**

```bash
./mvnw test -pl supply-svc -Dtest=AsyncConfigTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected:
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
```

If you see `setVirtualThreads` is unresolved: you are on JDK < 21 or Spring < 6.1. Confirm `java -version` returns 21.

- [ ] **Step 5: Run the full test suite to confirm no regressions**

```bash
./mvnw test -pl supply-svc
```

Expected: `BUILD SUCCESS`. `TestAsyncConfig` (the test-profile replacement) uses `SyncTaskExecutor` and references the same bean-name constants — it is unaffected by this change.

- [ ] **Step 6: Commit**

```bash
git add supply-svc/src/main/java/com/logistics/supply/configuration/AsyncConfig.java \
        supply-svc/src/test/java/com/logistics/supply/configuration/AsyncConfigTest.java
git commit -m "feat: migrate AsyncConfig to virtual threads, fix repository thread name prefix"
```

---

## Task 3: Enable virtual threads for Tomcat and default executor

**Files:**
- Modify: `supply-svc/src/main/resources/application.properties`

- [ ] **Step 1: Add the virtual-threads property**

At the end of the `application.async.*` block (after line 49), add:

```properties
spring.threads.virtual.enabled=true
```

The surrounding context for placement:
```properties
application.async.core-pool-size=5
application.async.max-pool-size=20
application.async.queue-capacity=100
spring.threads.virtual.enabled=true
```

The `application.async.*` properties are retained for backward compatibility but are now inert — `AsyncConfig` no longer reads them.

- [ ] **Step 2: Run the full test suite**

```bash
./mvnw test -pl supply-svc
```

Expected: `BUILD SUCCESS`. The property has no effect on the test context (`AsyncConfig` is excluded by `@Profile("!test")`; `TestAsyncConfig` uses `SyncTaskExecutor` regardless of this property).

- [ ] **Step 3: Verify the full multi-module build passes**

```bash
./mvnw clean package -Dmaven.test.skip=true
```

Expected: `BUILD SUCCESS` for both `supply-db` and `supply-svc`.

- [ ] **Step 4: Commit**

```bash
git add supply-svc/src/main/resources/application.properties
git commit -m "feat: enable virtual threads (spring.threads.virtual.enabled=true)"
```

---

## Verification Checklist

After all three tasks, verify:

- [ ] `java -version` on the CI runner reports `openjdk 21` (visible in GitHub Actions logs after pushing)
- [ ] `./mvnw test -pl supply-svc` passes locally with JDK 21
- [ ] Log output in dev/prod shows thread names like `taskExecutor-1` for `@Async` tasks (previously `taskExecutor-N` from `ThreadPoolTaskExecutor`, now virtual thread names from `SimpleAsyncTaskExecutor`)
- [ ] No `java.lang.IllegalStateException: ThreadPoolTaskExecutor not initialized` in logs after restart
