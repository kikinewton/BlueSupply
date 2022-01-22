package com.logistics.supply.configuration;

import com.logistics.supply.model.Employee;
import com.logistics.supply.repository.EmployeeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class AuditSecurityConfig {
  @Bean
  AuditorAware<Employee> auditorAware(EmployeeRepository repo) {
    // Lookup Employee instance corresponding to logged in user
    return () ->
        Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .filter(x -> Objects.nonNull(x))
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getName)
            .flatMap(repo::findByEmail);
  }
}
