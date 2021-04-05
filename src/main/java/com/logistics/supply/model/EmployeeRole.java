package com.logistics.supply.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Getter
@NoArgsConstructor
public enum EmployeeRole implements GrantedAuthority {
  ROLE_REGULAR,
  ROLE_HOD,
  ROLE_GENERAL_MANAGER,
  ROLE_PROCUREMENT_OFFICER,
  ROLE_STORE_OFFICER,
  ROLE_ACCOUNT_OFFICER,
  ROLE_ADMIN;

  @Override
  public String getAuthority() {
    return name();
  }

  //  @GeneratedValue(strategy = GenerationType.AUTO)
  //  @Id
  //  private int id;
  //
  //  @Column(unique = true)
  //  @Enumerated(EnumType.STRING)
  //  private EmployeeLevel employeeLevel;
  //
  //  @JsonIgnore
  //  @ManyToMany(mappedBy = "role")
  //  private Set<Employee> employees;
  //
  //  public EmployeeRole(EmployeeLevel employeeLevel) {
  //    this.employeeLevel = employeeLevel;
  //  }

}
