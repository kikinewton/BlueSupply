package com.logistics.supply.dto;

import com.logistics.supply.model.ItemDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FloatOrPettyCashDTO {

  List<ItemDTO> items;
}
