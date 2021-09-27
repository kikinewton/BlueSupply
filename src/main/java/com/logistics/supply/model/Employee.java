package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.logistics.supply.enums.EmployeeLevel;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Slf4j
@ToString
// @EntityListeners(AuditingEntityListener.class)
public class Employee {

  public Employee() {}

  public Employee(
      String firstName, String lastName, String password, String phoneNo, String email) {
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
  @Email
  private String email;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "department_id", referencedColumnName = "id")
  private Department department;

  @ElementCollection(fetch = FetchType.EAGER)
  @Size(max = 1)
  List<EmployeeRole> role;

  @Column private String fullName;

  @Column(updatable = false)
  @JsonSerialize
  Date createdAt = new Date();

  @JsonIgnore Date updatedAt;

  @JsonIgnore private Date lastLogin;

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
