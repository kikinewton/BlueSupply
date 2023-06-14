package com.logistics.supply.configuration;

import com.logistics.supply.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    private final EmployeeRepository employeeRepository;
    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event)
    {
        UserDetails user = (UserDetails) event.getAuthentication().getPrincipal();
        employeeRepository.updateLastLogin( user.getUsername());
        log.info("User %s login".formatted(user.getUsername()));
    }
}