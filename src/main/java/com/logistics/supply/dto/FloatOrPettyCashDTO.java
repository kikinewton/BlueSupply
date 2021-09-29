package com.logistics.supply.dto;

import com.logistics.supply.model.ItemDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FloatOrPettyCashDTO {

  @Size(min = 1)
  List<ItemDTO> items;
}
