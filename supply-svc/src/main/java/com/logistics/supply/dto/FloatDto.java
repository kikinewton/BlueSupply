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
public class FloatDto extends MinorDto {
  private String itemDescription;
  private BigDecimal estimatedUnitPrice;
  private int quantity;

  public static final FloatDto toDto(Floats floats) {
    FloatDto floatDTO = new FloatDto();
    BeanUtils.copyProperties(floats, floatDTO);
    return floatDTO;
  }
}
