package com.logistics.supply.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.Locale;

@UtilityClass
public class IdentifierUtil {

  public static String idHandler(String prefix, String department, int id) {
    String pre = prefix.substring(0, 3);
    String departmentPrefix = String.format("%-3s", department.substring(0, Math.min(3, department.length())))
                       .replace(' ', 'X');
    LocalDate today = LocalDate.now();
    String dayMonth = String.format("%02d%02d", today.getDayOfMonth(), today.getMonth().getValue());
    String paddedId = String.format("%08d", id);
    return (pre + "-" + departmentPrefix + "-" + paddedId + "-" + dayMonth).toUpperCase(Locale.ROOT);
  }
}
