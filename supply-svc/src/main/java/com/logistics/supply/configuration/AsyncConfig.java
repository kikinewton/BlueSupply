package com.logistics.supply.configuration;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

  public static final String TASK_EXECUTOR_REPOSITORY = "repositoryTaskExecutor";
  public static final String TASK_EXECUTOR_SERVICE = "serviceTaskExecutor";
  public static final String TASK_EXECUTOR_CONTROLLER = "controllerTaskExecutor";
  private static final String TASK_EXECUTOR_DEFAULT = "taskExecutor";
  private static final String TASK_EXECUTOR_NAME_PREFIX_DEFAULT = "taskExecutor-";
  private static final String TASK_EXECUTOR_NAME_PREFIX_REPOSITORY = "serviceTaskExecutor-";
  private static final String TASK_EXECUTOR_NAME_PREFIX_CONTROLLER = "controllerTaskExecutor-";
  private static final String TASK_EXECUTOR_NAME_PREFIX_SERVICE = "serviceTaskExecutor-";

  @Value("${application.async.core-pool-size}")
  int CORE_POOL_SIZE;
  @Value("${application.async.max-pool-size}")
  int MAX_POOL_SIZE;
  @Value("${application.async.queue-capacity}")
  int QUEUE_CAPACITY;

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
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(CORE_POOL_SIZE);
    executor.setMaxPoolSize(MAX_POOL_SIZE);
    executor.setQueueCapacity(QUEUE_CAPACITY);
    executor.setThreadNamePrefix(taskExecutorNamePrefix);
    return executor;
  }
}
