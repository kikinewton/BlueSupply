package com.logistics.supply.util;

import lombok.experimental.UtilityClass;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Locale;

@UtilityClass
public class IdentifierUtil {

  public static String idHandler(@NotBlank String prefix, String department, String id) {
    prefix = prefix.substring(0, 3);
    String _department =
        department.length() > 3
            ? department.substring(0, 3)
            : "O".concat(department);
    String dayOfMonth = String.valueOf(LocalDate.now().getDayOfMonth());
    String month = String.valueOf(LocalDate.now().getMonth().getValue());
    String dm = dayOfMonth.concat(month);
    String pd = prefix.concat("-").concat(_department);
    int len = 8;
    String newId = "";
    int diff = len - id.length();
    for (int i = 0; i < diff; i++) {
      if (newId.length() < diff) newId = newId.concat("0");
    }

    String revId = newId.concat(id);
    String result = pd.concat("-").concat(revId).concat("-").concat(dm).toUpperCase(Locale.ROOT);
    return result;
  }
}
