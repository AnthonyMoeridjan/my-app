package com.cofeecode.application.powerhauscore.data;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BillTest {

    @Test
    public void testBillCurrency() {
        Bill bill = new Bill();
        bill.setBillNumber("B-001");
        bill.setAmount(new BigDecimal("100.00"));
        bill.setCurrency(Currency.USD);
        bill.setBillDate(LocalDate.now());
        bill.setDueDate(LocalDate.now().plusDays(30));
        bill.setStatus(BillStatus.DRAFT);

        assertEquals(Currency.USD, bill.getCurrency());
    }
}