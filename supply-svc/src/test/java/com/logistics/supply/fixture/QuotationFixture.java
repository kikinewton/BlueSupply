package com.logistics.supply.fixture;

import com.logistics.supply.model.Quotation;

public class QuotationFixture {

    public QuotationFixture() {
    }

    public static Quotation getQuotation(int id) {
        Quotation quotation = new Quotation();
        quotation.setId(id);
        return quotation;
    }
}
