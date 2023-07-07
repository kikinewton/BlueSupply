package com.logistics.supply.fixture;

import com.logistics.supply.dto.ItemUpdateDto;

import java.math.BigDecimal;

public class ItemUpdateDtoFixture {

     ItemUpdateDtoFixture() {
    }

    public static ItemUpdateDto getItemUpdateDto() {
        return new ItemUpdateDto(2, "Paint bucket", BigDecimal.TEN);
    }
}
