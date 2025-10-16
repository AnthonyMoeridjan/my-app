package com.cofeecode.application.powerhauscore.views.accounting;

import com.cofeecode.application.powerhauscore.services.InvoiceService;
import com.cofeecode.application.powerhauscore.services.BillService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.cofeecode.application.powerhauscore.data.Bill;
import com.cofeecode.application.powerhauscore.data.Invoice;
import com.storedobject.chart.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Accounting Dashboard")
@Route(value = "accounting-dashboard", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class AccountingDashboardView extends VerticalLayout {

    private final InvoiceService invoiceService;
    private final BillService billService;

    public AccountingDashboardView(InvoiceService invoiceService, BillService billService) {
        this.invoiceService = invoiceService;
        this.billService = billService;
        addClassName("accounting-dashboard-view");
        setSizeFull();

        HorizontalLayout summaryCards = new HorizontalLayout();
        summaryCards.add(createCard("Total Open Invoices", invoiceService.getTotalOpenInvoices().toString()));
        summaryCards.add(createCard("Total Overdue Invoices", invoiceService.getTotalOverdueInvoices().toString()));
        summaryCards.add(createCard("Total Open Bills", billService.getTotalOpenBills().toString()));

        SOChart soChart = new SOChart();
        soChart.setSize("100%", "400px");

        // Create axes
        CategoryData labels = new CategoryData();
        Data values = new Data();
        invoiceService.getInvoiceAging().forEach((key, value) -> {
            labels.add(key);
            values.add(value);
        });

        XAxis xAxis = new XAxis(labels);
        YAxis yAxis = new YAxis(values);

        // Create the bar chart
        BarChart barChart = new BarChart(labels, values);
        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        barChart.plotOn(rc);

        // Add to the view
        soChart.add(barChart);

        Grid<Invoice> overdueInvoicesGrid = new Grid<>(Invoice.class, false);
        overdueInvoicesGrid.setItems(invoiceService.getOverdueInvoices());
        overdueInvoicesGrid.addColumn(Invoice::getInvoiceNumber).setHeader("Invoice Number");
        overdueInvoicesGrid.addColumn(Invoice::getAmount).setHeader("Amount");
        overdueInvoicesGrid.addColumn(Invoice::getDueDate).setHeader("Due Date");

        Grid<Bill> overdueBillsGrid = new Grid<>(Bill.class, false);
        overdueBillsGrid.setItems(billService.getOverdueBills());
        overdueBillsGrid.addColumn(Bill::getBillNumber).setHeader("Bill Number");
        overdueBillsGrid.addColumn(Bill::getAmount).setHeader("Amount");
        overdueBillsGrid.addColumn(Bill::getDueDate).setHeader("Due Date");

        add(summaryCards, soChart, new Span("Overdue Invoices"), overdueInvoicesGrid, new Span("Overdue Bills"), overdueBillsGrid);
    }

    private VerticalLayout createCard(String title, String value) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("summary-card");
        card.add(new Span(title));
        card.add(new Span(value));
        return card;
    }
}
