package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FloatOrPettyCashDTO {

  List<ItemDTO> items;
  private String requestedBy;
  private String requestedByPhoneNo;
  private BigDecimal amount;
  private String description;
  private String staffId;
}
