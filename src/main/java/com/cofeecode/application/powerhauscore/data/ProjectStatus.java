package com.cofeecode.application.powerhauscore.data;

public enum ProjectStatus {
    LEAD("Lead"),
    QUOTED("Quoted"),
    APPROVED("Approved"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    INVOICED("Invoiced"),
    PAID("Paid"),
    LOST_CANCELED("Lost/Canceled");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
