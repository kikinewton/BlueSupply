package com.logistics.supply.auth;

import com.logistics.supply.model.Employee;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class AppUserDetails implements UserDetails {

  private Employee employee;

  private Collection<? extends GrantedAuthority> authorities;

  public AppUserDetails(Employee employee, Collection<? extends GrantedAuthority> authorities) {
    this.employee = employee;
    this.authorities = authorities;
  }

//  @Override
//  public Collection<? extends GrantedAuthority> getAuthorities() {
//    List<GrantedAuthority> authorities =
//        employee.getRoles().stream()
//            .map(x -> new SimpleGrantedAuthority(x.getPrivileges())).filter(Objects::nonNull)
//            .collect(Collectors.toList());
//    return authorities;
//  }
//
//  public static AppUserDetails build(Employee employee) {
//    List<GrantedAuthority> authorities =
//        employee.getRoles().stream()
//            .map(role -> new SimpleGrantedAuthority(role.getPrivileges()))
//            .collect(Collectors.toList());
//
//    return new AppUserDetails(employee, authorities);
//  }



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
