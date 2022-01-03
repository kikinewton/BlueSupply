package com.logistics.supply.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class PettyCashOrder extends AbstractAuditable<Employee, Integer> {

  @OneToMany(
      mappedBy = "pettyCashOrder",
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private Set<PettyCash> pettyCash = new HashSet<>();

  private String requestedBy;
  private String requestedByPhoneNo;
  private String pettyCashOrderRef;


  public void addPettyCash(PettyCash _pettyCash) {
    this.pettyCash.add(_pettyCash);
    _pettyCash.setPettyCashOrder(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    PettyCashOrder that = (PettyCashOrder) o;
    return Objects.equals(pettyCash, that.pettyCash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), pettyCash);
  }
}
