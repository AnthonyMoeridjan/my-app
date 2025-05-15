package com.cofeecode.application.powerhauscore.data;

public enum PersonType {
    CUSTOMER("Customer"),
    EMPLOYEE_FULLTIME("Employee"),
    EMPLOYEE_PARTTIME("PartimeEmployee"),
    SUPPLIER("Supplier"),
    PARTNER("Partner"),
    OTHER("Other");

    private final String label;

    PersonType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
