package com.logistics.supply.dto;

import com.logistics.supply.model.Floats;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class FloatDTO extends MinorDTO{
  private String itemDescription;
  private BigDecimal estimatedUnitPrice;
  private int quantity;

  public static final FloatDTO toDto(Floats floats) {
    FloatDTO floatDTO = new FloatDTO();
    BeanUtils.copyProperties(floats, floatDTO);
    return floatDTO;
  }
}
