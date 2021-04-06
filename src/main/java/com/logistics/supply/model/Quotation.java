package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.AbstractAuditable;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Data
@JsonIgnoreProperties(
    value = {"createdDate", "lastModifiedDate", "createdBy", "lastModifiedBy", "new"})
public class Quotation extends AbstractAuditable<Employee, Integer> {

  @ManyToMany(mappedBy = "quotations")
  private Set<RequestItem> requestItems;

  @ManyToOne private Supplier supplier;

  @ElementCollection(fetch = FetchType.EAGER)
  private Set<RequestDocument> requestDocument;
}
