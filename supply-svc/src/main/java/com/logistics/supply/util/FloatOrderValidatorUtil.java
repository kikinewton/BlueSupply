package com.logistics.supply.util;

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
}
