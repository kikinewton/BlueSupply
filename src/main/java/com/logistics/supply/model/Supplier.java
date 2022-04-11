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

  @Column(nullable = false, unique = true)
  @ValidName
  private String name;

  private String phone_no;

  private String location;

  @NotBlank
  @ValidDescription
  private String description;

  @Email
  private String email;

  String accountNumber;

  String bank;

  boolean registered;

}
