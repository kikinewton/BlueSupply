package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.logistics.supply.enums.EmployeeLevel;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.Date;

@Data
@Entity
@Slf4j
@ToString
public class Employee {


  public Employee() {
  }

  public Employee(String firstName, String lastName, String password, String phoneNo, String email) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
    this.phoneNo = phoneNo;
    this.email = email;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(unique = true, length = 50)
  private Integer id;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(nullable = false)
  @JsonIgnore
  @ToString.Exclude
  private String password;

  @Column(nullable = false)
  private String phoneNo;

  @Column(name = "enabled")
  Boolean enabled;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private EmployeeLevel employeeLevel;

  @Column(nullable = false)
  @Email
  private String email;

  //    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "employee")
  //    private List<RequestItem> requestItem = new ArrayList<>();
  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "department_id", referencedColumnName = "id")
  private Department department;

//  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//  @JoinTable(name = "emp_role", joinColumns = @JoinColumn(name = "employee_id"), inverseJoinColumns = @JoinColumn(name = "emp_role_id"))
  private String roles;

  @Column private String fullName;

  @Column(updatable = false)
  @JsonSerialize
  Date createdAt = new Date();

  @JsonIgnore Date updatedAt;

  @PrePersist
  public void logNewEmployeeAttempt() {
    log.info("Attempting to add new user with phoneNo: " + phoneNo);
  }

  @PostPersist
  public void logNewEmployeeAdded() {
    log.info("Added user '" + firstName + "' with ID: " + id);
  }

  @PreRemove
  public void logEmployeeRemovalAttempt() {
    log.info("Attempting to delete user: " + fullName);
  }

  @PostRemove
  public void logEmployeeRemoval() {
    log.info("Deleted user: " + phoneNo);
  }

  @PreUpdate
  public void logEmployeeUpdateAttempt() {
    log.info("Attempting to update user: " + phoneNo);
  }

  @PostUpdate
  public void logEmployeeUpdate() {
    log.info("Updated user: " + phoneNo);
  }

  @PostLoad
  public void logEmployeeLoad() {
    fullName = firstName + " " + lastName;
  }


}
