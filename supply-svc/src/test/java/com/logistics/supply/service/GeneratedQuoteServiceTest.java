package com.logistics.supply.service;

import com.logistics.supply.common.annotations.IntegrationTest;
import com.logistics.supply.dto.ItemUpdateDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@IntegrationTest
class GeneratedQuoteServiceTest {

    @Autowired
    GeneratedQuoteService generatedQuoteService;

    @Test
    void testComposeProductDescription() {

        List<ItemUpdateDto> items = new ArrayList<>();
        items.add(new ItemUpdateDto(3, "512GB SSD SATA", BigDecimal.valueOf(900)));
        items.add(new ItemUpdateDto(1, "512GB SSD SATA", BigDecimal.valueOf(900)));


        String description = generatedQuoteService.composeProductDescription(items);
        String expected = "(quantity=3, description=512GB SSD SATA, estimatedPrice=900\n" +
                          "(quantity=1, description=512GB SSD SATA, estimatedPrice=900";

        Assertions.assertEquals(expected, description);
    }
}