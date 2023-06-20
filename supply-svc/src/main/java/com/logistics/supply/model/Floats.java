package com.logistics.supply.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "float")
public class Floats  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 30)
  private String floatRef;

  @ManyToOne
  @JoinColumn(name = "department_id")
  private Department department;

  @PositiveOrZero private BigDecimal estimatedUnitPrice;

  @NotBlank private String itemDescription;

  @PositiveOrZero private int quantity;

  private boolean flagged = Boolean.FALSE;


  @CreationTimestamp
  private Date createdDate;

  @UpdateTimestamp
  private Date updatedDate;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "created_by_id")
  private Employee createdBy;


  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "float_order_id")
  private FloatOrder floatOrder;


}
