package com.logistics.supply.fixture;

import com.logistics.supply.dto.GeneratedQuoteDto;
import com.logistics.supply.dto.ItemUpdateDto;
import com.logistics.supply.model.Supplier;

import java.util.Date;
import java.util.List;

public class GeneratedQuoteDtoFixture {
     GeneratedQuoteDtoFixture() {
    }

    public static GeneratedQuoteDto generatedQuoteDto() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDtoFixture.getItemUpdateDto();
        Supplier supplier = SupplierFixture.getSupplier("Jilorm");
        List<ItemUpdateDto> itemUpdateDtoList = List.of(itemUpdateDto);
        GeneratedQuoteDto generatedQuoteDto = new GeneratedQuoteDto();
        generatedQuoteDto.setLocation("Accra");
        generatedQuoteDto.setPhoneNo("0039494303");
        generatedQuoteDto.setItems(itemUpdateDtoList);
        generatedQuoteDto.setSupplier(supplier);
        generatedQuoteDto.setDeliveryDate(String.valueOf(new Date()));
        generatedQuoteDto.setProductDescription("");
        return generatedQuoteDto;
    }

}
