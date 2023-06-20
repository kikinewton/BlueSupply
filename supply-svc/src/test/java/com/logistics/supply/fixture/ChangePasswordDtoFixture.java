package com.logistics.supply.fixture;

import com.logistics.supply.dto.ChangePasswordDto;

public class ChangePasswordDtoFixture {
  ChangePasswordDtoFixture() {}

  public static ChangePasswordDto getValidPasswordDto() {
    return new ChangePasswordDto("wZ7f8OSMsV", "Password1.(0m");
  }

  public static ChangePasswordDto getInvalidPasswordDto() {
    return new ChangePasswordDto("password", "Password1.(0m");
  }
}
