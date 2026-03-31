package com.logistics.supply.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.logistics.supply.model.PettyCash;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PettyCashDto extends MinorDto {
    private ItemDto item;

    public static final PettyCashDto toDto(PettyCash pettyCash) {
        PettyCashDto pettyCashDTO = new PettyCashDto();
        // item is null here — this method throws NullPointerException (existing bug preserved)
        ItemDto target = pettyCashDTO.getItem();
        target.setName(pettyCash.getName());
        target.setPurpose(pettyCash.getPurpose());
        target.setQuantity(pettyCash.getQuantity());
        return pettyCashDTO;
    }
}
