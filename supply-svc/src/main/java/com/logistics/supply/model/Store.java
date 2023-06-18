package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Store extends AbstractAuditable<Employee, Integer> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(length = 15)
  private String name;

  public Store(String name) {
    this.name = name;
  }
}
