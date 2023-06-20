package com.logistics.supply.fixture;

import com.logistics.supply.dto.ItemDto;
import com.logistics.supply.model.RequestDocument;

import java.math.BigDecimal;
import java.util.List;

public class ItemDtoFixture {

    public static ItemDto createSampleItemDto(
            String itemName,
            BigDecimal unitPrice) {

        ItemDto itemDto = new ItemDto();

        // Set the list of documents in the itemDto
        RequestDocument requestDocument = RequestDocumentFixture.getRequestDocument("receipt");
        List<RequestDocument> documents = List.of(requestDocument);
        // Set values for the other properties of the itemDto
        itemDto.setName(itemName);
        itemDto.setPurpose("Sample purpose");
        itemDto.setQuantity(1);
        itemDto.setUnitPrice(unitPrice);
        itemDto.setDocuments(documents);

        return itemDto;
    }
}

