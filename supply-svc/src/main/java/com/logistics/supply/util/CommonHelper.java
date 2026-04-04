package com.logistics.supply.util;

import com.logistics.supply.exception.PasswordMismatchException;
import com.logistics.supply.model.RequestItem;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class CommonHelper {


  private static final String EMAIL_REGEX =
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

  public static boolean isValidEmailAddress(String email) {
    return email != null && email.matches(EMAIL_REGEX);
  }

  public static void matchBCryptPassword(String hashedPassword, String rawPassword) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    boolean matches = encoder.matches(rawPassword, hashedPassword);
    if (!matches) {
      throw new PasswordMismatchException();
    }
  }

  public static String buildHtmlTableForRequestItems(List<String> title, List<RequestItem> items) {
    StringBuilder header = new StringBuilder();
    for (String t : title) header.append(String.format(Constants.tableHeader, t));
    header = new StringBuilder(String.format(Constants.tableRow, header));
    String ri =
        items.stream()
            .map(
                i ->
                    String.format(Constants.tableData, i.getName())
                        + String.format(Constants.tableData, i.getQuantity())
                        + String.format(Constants.tableData, i.getReason())
                        + String.format(Constants.tableData, i.getPurpose()))
            .map(j -> String.format(Constants.tableRow, j))
            .collect(Collectors.joining("", "", ""));
    return header.toString().concat(ri);
  }

}
