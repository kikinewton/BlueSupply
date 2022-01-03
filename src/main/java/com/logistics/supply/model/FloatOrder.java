package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@JsonIgnoreProperties(
    value = {"lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class FloatOrder extends AbstractAuditable<Employee, Integer> {

  @OneToMany(
      mappedBy = "floatOrder",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private Set<Floats> floats = new HashSet<>();

  private boolean retired;
  private String floatOrderRef;
  private String requestedBy;
  private String requestedByPhoneNo;
  private BigDecimal amount;
  private String description;

  public FloatOrder(Set<Floats> floats) {
    this.floats = floats;
  }

  public void addFloat(Floats _floats) {
    this.floats.add(_floats);
    _floats.setFloatOrder(this);
  }
}
