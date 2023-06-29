package com.logistics.supply.fixture;

import com.logistics.supply.dto.ItemUpdateDto;

public class ItemUpdateDtoFixture {

     ItemUpdateDtoFixture() {
    }

    public static ItemUpdateDto getItemUpdateDto() {
        return new ItemUpdateDto(2, "Paint bucket", null);
    }
}
