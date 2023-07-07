package com.logistics.supply.dto;

import lombok.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CreateQuotationRequest {

  @Positive int supplierId;

  @Size(min = 1)
  Set<Integer> requestItemIds;

  int documentId;
}
