package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.logistics.supply.event.listener.EmployeeListener;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Slf4j
@ToString
@EntityListeners(EmployeeListener.class)
public class Employee {

  @Column(name = "enabled")
  Boolean enabled;

  @ManyToMany
  @JoinTable(
      name = "employee_role",
      joinColumns = @JoinColumn(name = "employee_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
  @Size(max = 1)
  List<Role> roles;

  @CreationTimestamp @JsonSerialize Date createdAt;

  @JsonIgnore Date updatedAt;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(unique = true, length = 50)
  private Integer id;

  @Column(nullable = false, length = 30)
  private String firstName;

  @Column(nullable = false, length = 30)
  private String lastName;

  @Column(nullable = false)
  @JsonIgnore
  @ToString.Exclude
  private String password;

  @Column(nullable = false, length = 100)
  private String phoneNo;

  @Column(nullable = false, length = 20)
  @Email
  private String email;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "department_id", referencedColumnName = "id")
  private Department department;

  @Column(length = 50) private String fullName;

  @JsonIgnore private Date lastLogin;

  private boolean changedDefaultPassword;

  public Employee() {}

  public Employee(
      String firstName, String lastName, String password, String phoneNo, String email) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
    this.phoneNo = phoneNo;
    this.email = email;
  }

  @PrePersist
  public void logNewEmployeeAttempt() {
    log.info("Attempting to add new user with phoneNo: " + phoneNo);
  }

  @PostPersist
  public void logNewEmployeeAdded() {
    log.info("Added user '" + firstName + "' with email: " + email);
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
    updatedAt = new Date();
    log.info("Updated user: " + phoneNo);
  }

  @PostLoad
  public void logEmployeeLoad() {
    fullName = firstName + " " + lastName;
  }
}
