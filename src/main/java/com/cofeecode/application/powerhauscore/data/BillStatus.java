package com.cofeecode.application.powerhauscore.data;

public enum BillStatus {
    DRAFT("Draft"),
    RECEIVED("Received"),
    PAID("Paid");

    private final String displayName;

    BillStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
