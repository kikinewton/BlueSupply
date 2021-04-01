package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logistics.supply.enums.EmployeeLevel;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class EmployeeRole {

  @GeneratedValue(strategy = GenerationType.AUTO)
  @Id
  private int id;

  @Column(unique = true)
  @Enumerated(EnumType.STRING)
  private EmployeeLevel employeeLevel;

  @JsonIgnore
  @ManyToMany(mappedBy = "role")
  private Set<Employee> employees;

  public EmployeeRole(EmployeeLevel employeeLevel) {
    this.employeeLevel = employeeLevel;
  }


}
