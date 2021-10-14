package com.logistics.supply.dto;

import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;
import com.logistics.supply.model.RequestDocument;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Set;

@Getter
@ToString
public class ItemDTO {

  @Column(nullable = false, updatable = false)
  @ValidName
  private String name;
  @Column(nullable = false, updatable = false)
  @ValidDescription
  private String purpose;
  @Positive private Integer quantity;
  @PositiveOrZero private BigDecimal unitPrice;
  Set<RequestDocument> documents;
}
