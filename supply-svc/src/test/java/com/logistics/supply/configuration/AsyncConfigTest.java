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
        assertTrue(
            serviceThreadName.get().startsWith("serviceTaskExecutor-"),
            "Service executor thread name should start with 'serviceTaskExecutor-'. Got: " + serviceThreadName.get()
        );
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
