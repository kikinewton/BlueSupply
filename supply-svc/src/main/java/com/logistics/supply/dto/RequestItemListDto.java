package com.logistics.supply.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.logistics.supply.model.RequestItem;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class RequestItemListDto {

  @Size(min = 1)
  private List<RequestItem> items;

  @FutureOrPresent
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date deliveryDate;

  private int quotationId;
}
