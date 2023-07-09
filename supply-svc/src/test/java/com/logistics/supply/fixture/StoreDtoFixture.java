package com.logistics.supply.fixture;

import com.logistics.supply.dto.StoreDto;

public class StoreDtoFixture {

     StoreDtoFixture() {
    }

    public static StoreDto getStoreDto(String name) {
        StoreDto storeDto = new StoreDto();
        storeDto.setName(name);
        return storeDto;
    }
}
