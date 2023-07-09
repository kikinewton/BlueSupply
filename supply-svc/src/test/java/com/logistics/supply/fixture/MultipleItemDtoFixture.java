package com.logistics.supply.fixture;

import com.logistics.supply.dto.LpoMinorRequestItem;
import com.logistics.supply.dto.MultipleItemDto;
import com.logistics.supply.model.Department;

import java.util.List;
import java.util.stream.Collectors;

public class MultipleItemDtoFixture {

  MultipleItemDtoFixture() {}

    public static MultipleItemDto getMultipleGoodsRequestItems() {
        List<String> items = List.of("Book", "Pen", "Bag");
        Department department = DepartmentFixture.getDepartment("IT");
        List<LpoMinorRequestItem> lpoMinorRequestItems = items.stream()
                .map(i -> LpoRequestItemFixture.getGoodsRequestItem(i, 1, department))
                .collect(Collectors.toList());
        return new MultipleItemDto(lpoMinorRequestItems);
    }
}
