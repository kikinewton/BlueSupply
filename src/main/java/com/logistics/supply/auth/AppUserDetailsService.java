package com.logistics.supply.auth;

import com.logistics.supply.model.Employee;
import com.logistics.supply.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService {

  private static final String USER_NOT_FOUND_MSG = "Employee with email %s not found";

  @Autowired private EmployeeRepository employeeRepository;
  @Autowired BCryptPasswordEncoder bCryptPasswordEncoder;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Employee employee =
        employeeRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));

    return AppUserDetails.build(employee);
  }
}
