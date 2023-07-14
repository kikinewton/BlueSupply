package com.logistics.supply.util;

import com.logistics.supply.exception.RetireFloatOrderException;
import com.logistics.supply.model.FloatOrder;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;

public class FloatOrderValidatorUtil {

     FloatOrderValidatorUtil() {
    }

    public static boolean validateDueNonRetiredFloatOrder(FloatOrder floatOrder, int numberOfDaysToRetire) {
        return floatOrder.getCreatedDate()
                .plusDays(numberOfDaysToRetire)
                .isAfter(ChronoLocalDate.from(LocalDateTime.now()));
    }

    public static void isRetirementApprovedByGeneralManager(FloatOrder floatOrder) {
         if (Boolean.FALSE.equals(floatOrder.getAuditorRetirementApproval())) {
             String message = "Float order with id %s not approved for retirement by General manager"
                     .formatted(floatOrder.getId());
             throw new RetireFloatOrderException(message);
         }
     }
}
