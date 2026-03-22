package com.logistics.supply.enums;

public enum RequestStageLabel {

    LPO_ISSUED("LPO ISSUED"),
    GRN_ISSUED("GRN ISSUED"),
    GRN_HOD_ENDORSED("GRN HOD ENDORSED"),
    PROCUREMENT_PAYMENT_ADVICE("PROCUREMENT PAYMENT ADVICE"),
    ACCOUNT_INITIATED_PAYMENT("ACCOUNT INITIATED PAYMENT"),
    AUDITOR_PAYMENT_CHECK("AUDITOR PAYMENT CHECK"),
    FM_PAYMENT_AUTHORIZATION("FM PAYMENT AUTHORIZATION"),
    GM_PAYMENT_APPROVAL("GM PAYMENT APPROVAL");

    private final String label;

    RequestStageLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}