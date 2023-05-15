package com.logistics.supply.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Date;

@UtilityClass
public class FloatReportUtil {

  public FloatReportDto mapArrayToDto(Object[] obj) {
    FloatReportDto f = new FloatReportDto();
    f.setFloatRef((String) obj[0]);
    f.setDescription((String) obj[1]);
    f.setAmount((BigDecimal) obj[2]);
    f.setDepartment((String) obj[3]);
    f.setCreatedBy((String) obj[4]);
    f.setRequestedBy(String.valueOf(obj[5]));
    f.setRequestedByPhoneNumber(String.valueOf(obj[6]));
    f.setCreatedDate((Date) obj[7]);
    f.setAgeingValue((Integer) obj[8]);
    f.setRetired((Boolean) obj[9]);
    return f;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class FloatReportDto {
    private String floatRef;
    private String description;
    private BigDecimal amount;
    private String department;
    private String createdBy;
    private String requestedBy;
    private String requestedByPhoneNumber;
    private Date createdDate;
    private int ageingValue;
    private boolean retired;
  }
}
