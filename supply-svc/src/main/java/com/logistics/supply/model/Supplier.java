package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@Setter
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Supplier extends AbstractAuditable<Employee, Integer> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(nullable = false, unique = true, length = 50)
  @ValidName
  private String name;

  @Column(length = 15)
  private String phoneNo;

  @Column(length = 30)
  private String location;

  @NotBlank
  @ValidDescription
  @Column(length = 50)
  private String description;

  @Email
  @Column(length = 40)
  private String email;

  @Column(length = 20)
  private String accountNumber;

  @Column(length = 30)
  private String bank;

  private boolean registered;
}
