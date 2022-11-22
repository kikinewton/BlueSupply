package com.logistics.supply.annotation.validator;

import com.logistics.supply.TestUtil;
import com.logistics.supply.model.RequestItem;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ApprovedValidatorTest {

    @Test
    void verifyRequestApprovalStatus() throws IOException {
        RequestItem item = TestUtil.requestItem("vo/unapproved-request-item.json");
        boolean approved = ApprovedValidator.isApproved(item);
        assertFalse(approved);
    }
}