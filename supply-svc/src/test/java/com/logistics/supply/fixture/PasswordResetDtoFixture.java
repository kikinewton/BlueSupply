package com.logistics.supply.fixture;

import com.logistics.supply.dto.PasswordResetDto;

public class PasswordResetDtoFixture {

  PasswordResetDtoFixture() {}

  public static PasswordResetDto getPasswordResetDto(String email) {
    return new PasswordResetDto(
            "token",
            "12@()kindWords.org",
            email);
  }

  public static PasswordResetDto getPasswordResetDtoWithEmptyPassword(String email) {
    return new PasswordResetDto(
            "token",
            "",
            email);
  }
}
