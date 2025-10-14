package com.cofeecode.application.powerhauscore.data;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvoiceTest {

    @Test
    public void testInvoiceCurrency() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("I-001");
        invoice.setAmount(new BigDecimal("200.00"));
        invoice.setCurrency(Currency.EUR);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(InvoiceStatus.DRAFT);

        assertEquals(Currency.EUR, invoice.getCurrency());
    }
}