package com.logistics.supply.auth;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.logistics.supply.model.Employee;
import java.util.Collection;

@Data
public class AppUserDetails implements UserDetails {

  private Employee employee;

  private Collection<? extends GrantedAuthority> authorities;

  public AppUserDetails(Employee employee, Collection<? extends GrantedAuthority> authorities) {
    this.employee = employee;
    this.authorities = authorities;
  }

  @Override
  public String getPassword() {
    return employee.getPassword();
  }

  @Override
  public String getUsername() {
    return employee.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
