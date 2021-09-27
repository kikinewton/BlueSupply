package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.logistics.supply.enums.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class PettyCash extends AbstractAuditable<Employee, Integer> {

  @PositiveOrZero BigDecimal amount;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column
  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;


  @ManyToOne
  @JoinColumn(name = "department_id")
  Department department;

  String pettyCashRef;


}
