package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@Setter
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Supplier extends AbstractAuditable<Employee, Integer> {

  @Column(nullable = false, unique = true, length = 20)
  @ValidName
  private String name;

  @Column(length = 15)
  private String phone_no;

  @Column(length = 30)
  private String location;

  @NotBlank
  @ValidDescription
  @Column(length = 50)
  private String description;

  @Email
  @Column(length = 20)
  private String email;

  @Column(length = 20)
  String accountNumber;

  @Column(length = 20)
  String bank;

  boolean registered;

}
