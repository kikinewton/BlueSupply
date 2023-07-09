package com.logistics.supply.fixture;

import com.logistics.supply.dto.FloatOrPettyCashDto;
import com.logistics.supply.dto.ItemDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class FloatOrPettyCashDtoFixture {

  FloatOrPettyCashDtoFixture() {}

  public static FloatOrPettyCashDto getBulkFloat() {
    List<String> itemNames = List.of("Chair", "HDMI cord");
    List<ItemDto> items = itemNames.stream()
            .map(i -> ItemDtoFixture.createSampleItemDto(i, BigDecimal.TEN))
            .collect(Collectors.toList());

    FloatOrPettyCashDto floatOrPettyCashDto = new FloatOrPettyCashDto();
    floatOrPettyCashDto.setRequestedBy("Theodore Adams");
    floatOrPettyCashDto.setRequestedByPhoneNo("903-870-9586");
    floatOrPettyCashDto.setStaffId("P0993");
    floatOrPettyCashDto.setItems(items);
    return floatOrPettyCashDto;
  }

  public static FloatOrPettyCashDto getBulkPettyCash() {
    List<String> itemNames = List.of("USB", "Brake fluid");
    List<ItemDto> items = itemNames.stream()
            .map(i -> ItemDtoFixture.createSampleItemDto(i, BigDecimal.TEN))
            .collect(Collectors.toList());

    FloatOrPettyCashDto floatOrPettyCashDto = new FloatOrPettyCashDto();
    floatOrPettyCashDto.setRequestedBy("Bobby Taylor");
    floatOrPettyCashDto.setRequestedByPhoneNo("903-870-9586");
    floatOrPettyCashDto.setStaffId("P083");
    floatOrPettyCashDto.setItems(items);
    return floatOrPettyCashDto;
  }
}
