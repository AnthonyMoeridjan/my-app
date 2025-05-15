package com.cofeecode.application.powerhauscore.customfield;

import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.html.Div;

public class CustomFieldSizeVariants extends Div {

    public CustomFieldSizeVariants() {
        PriceField priceField = new PriceField("Price");
        priceField.addThemeVariant(CustomFieldVariant.LUMO_SMALL);
        add(priceField);
    }

}
