package com.logistics.supply.common.config;

import com.logistics.supply.configuration.AsyncConfig;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Replaces AsyncConfig (excluded via @Profile("!test")) in the test profile.
 *
 * Uses SyncTaskExecutor so all @Async event listeners run in the calling thread,
 * completing before MockMvc.perform() returns. This eliminates deadlocks between
 * in-flight listener transactions and the AFTER_TEST_METHOD TRUNCATE cleanup.
 */
@TestConfiguration
public class TestAsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean(name = AsyncConfig.TASK_EXECUTOR_REPOSITORY)
    public Executor repositoryTaskExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean(name = AsyncConfig.TASK_EXECUTOR_SERVICE)
    public Executor serviceTaskExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean(name = AsyncConfig.TASK_EXECUTOR_CONTROLLER)
    public Executor controllerTaskExecutor() {
        return new SyncTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
