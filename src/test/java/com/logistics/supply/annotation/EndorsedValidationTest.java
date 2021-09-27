package com.logistics.supply.annotation;

import com.logistics.supply.enums.EndorsementStatus;
import com.logistics.supply.model.RequestItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.logistics.supply.annotation.validator.EndorsedValidator.isEndorsed;

public class EndorsedValidationTest {

    public void init() {
        RequestItem r = new RequestItem();
        r.setEndorsement(EndorsementStatus.ENDORSED);
        r.setName("Pencil");
        r.setQuantity(1);
    }

    @Test
    public void isEndorsedTest() {
        RequestItem r = new RequestItem();
        r.setEndorsement(EndorsementStatus.ENDORSED);
        r.setName("Pencil");
        r.setQuantity(1);

        Assertions.assertTrue(isEndorsed(r));
    }
}
