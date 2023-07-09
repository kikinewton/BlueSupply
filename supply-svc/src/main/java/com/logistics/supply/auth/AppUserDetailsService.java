package com.logistics.supply.auth;

import com.logistics.supply.model.Privilege;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.supply.model.Employee;
import com.logistics.supply.model.Role;
import com.logistics.supply.repository.EmployeeRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

  private static final String USER_NOT_FOUND_MSG = "Employee with email %s not found";

  private final EmployeeRepository employeeRepository;

  @Override
  @Transactional
  @Cacheable(value = "userDetails", key = "#email")
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Employee employee =
        employeeRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));

    return new org.springframework.security.core.userdetails.User(
            employee.getEmail(), employee.getPassword(), true, true, true,
            true, getAuthorities(employee.getRoles()));
  }

  @Cacheable(value = "authorities", unless = "#result.isEmpty == true")
  private Collection<? extends GrantedAuthority> getAuthorities(
          Collection<Role> roles) {

    return getGrantedAuthorities(getPrivileges(roles));
  }
  @Cacheable(value = "privileges", unless = "#result.isEmpty == true")
  private List<String> getPrivileges(Collection<Role> roles) {

    List<String> privileges = new ArrayList<>();
    List<Privilege> collection = new ArrayList<>();
    for (Role role : roles) {
      privileges.add(role.getName());
      collection.addAll(role.getPrivileges());
    }
    for (Privilege item : collection) {
      privileges.add(item.getName());
    }
    return privileges;
  }
  @Cacheable(value = "grantAuthorities", unless = "#result.isEmpty == true")
  private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String privilege : privileges) {
      authorities.add(new SimpleGrantedAuthority(privilege));
    }
    return authorities;
  }
}
