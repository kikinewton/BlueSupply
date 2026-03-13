package com.logistics.supply.common.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Waits for ForkJoinPool.commonPool() to be quiescent before afterEach completes.
 *
 * Services in this application use CompletableFuture.runAsync(task) without supplying
 * an executor, which submits tasks to ForkJoinPool.commonPool() outside Spring's
 * executor management. These tasks hold DB transactions. If the AFTER_TEST_METHOD
 * TRUNCATE cleanup fires while a task is still running, PostgreSQL detects a deadlock.
 *
 * Because JUnit 5 calls AfterEachCallbacks in reverse registration order (LIFO),
 * this extension must be registered AFTER SpringExtension so that it runs BEFORE
 * SpringExtension.afterEach() (which triggers SqlScriptsTestExecutionListener and the TRUNCATE).
 * Placing @ExtendWith(AsyncCompletionExtension.class) after @SpringBootTest in the
 * composed @IntegrationTest annotation achieves this ordering.
 */
public class AsyncCompletionExtension implements AfterEachCallback {

    private static final long TIMEOUT_SECONDS = 10;

    @Override
    public void afterEach(ExtensionContext context) {
        ForkJoinPool.commonPool().awaitQuiescence(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
