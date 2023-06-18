package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import com.logistics.supply.model.PettyCash;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PettyCashDto extends MinorDto {
    private ItemDTO item;

    public static final PettyCashDto toDto(PettyCash pettyCash) {
        PettyCashDto pettyCashDTO = new PettyCashDto();
        BeanUtils.copyProperties(pettyCash, pettyCashDTO.getItem());
        return pettyCashDTO;
    }
}
