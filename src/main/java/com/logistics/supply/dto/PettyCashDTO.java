package com.logistics.supply.dto;

import com.logistics.supply.model.PettyCash;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PettyCashDTO extends MinorDTO {
    ItemDTO item;

    public static final PettyCashDTO toDto(PettyCash pettyCash) {
        PettyCashDTO pettyCashDTO = new PettyCashDTO();
        BeanUtils.copyProperties(pettyCash, pettyCashDTO.getItem());
        return pettyCashDTO;
    }
}
