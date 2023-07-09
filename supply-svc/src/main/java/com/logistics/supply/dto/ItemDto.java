package com.logistics.supply.dto;

import com.logistics.supply.annotation.ValidDescription;
import com.logistics.supply.annotation.ValidName;
import com.logistics.supply.model.RequestDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

@Getter
@ToString
@Setter
@NoArgsConstructor
public class ItemDto {

  private List<RequestDocument> documents;

  @ValidName
  private String name;

  @ValidDescription
  private String purpose;

  @Positive private Integer quantity;

  @PositiveOrZero private BigDecimal unitPrice;
  
}
