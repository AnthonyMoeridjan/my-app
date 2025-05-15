package com.cofeecode.application.powerhauscore.customfield;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;

public class PriceField extends CustomField<Money> {

    private TextField amount;
    private Select<String> currency;

    public PriceField(String label) {
        this();
        setLabel(label);
    }

    public PriceField() {
        amount = new TextField();
        amount.setPlaceholder("0.00");
        // Sets title for screen readers
        amount.setAriaLabel("Amount");

        currency = new Select<>();
        currency.setItems("SRD", "EUR", "USD");
        currency.setWidth("6em");
        currency.setAriaLabel("Currency");
        currency.setValue("SRD");

        HorizontalLayout layout = new HorizontalLayout(amount, currency);
        // Removes default spacing
        layout.setSpacing(false);
        // Adds small amount of space between the components
        layout.getThemeList().add("spacing-s");

        add(layout);
    }

    public void addThemeVariant(CustomFieldVariant variant) {
        super.addThemeVariants(variant);
        amount.addThemeVariants(TextFieldVariant.valueOf(variant.name()));
        currency.addThemeVariants(SelectVariant.valueOf(variant.name()));
    }

    @Override
    protected Money generateModelValue() {
        return new Money(amount.getValue(), currency.getValue());
    }

    @Override
    protected void setPresentationValue(Money money) {
        amount.setValue(money.getAmount());
        currency.setValue(money.getCurrency());
    }

    public TextField getAmount() {
        return amount;
    }

    public Select<String> getCurrency() {
        return currency;
    }
}