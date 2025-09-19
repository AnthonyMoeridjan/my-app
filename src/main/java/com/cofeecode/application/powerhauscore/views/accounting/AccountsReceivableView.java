package com.cofeecode.application.powerhauscore.views.accounting;

import com.cofeecode.application.powerhauscore.data.Invoice;
import com.cofeecode.application.powerhauscore.services.InvoiceService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;

@PageTitle("Accounts Receivable")
@Route(value = "accounts-receivable", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class AccountsReceivableView extends VerticalLayout {

    private final InvoiceService invoiceService;

    private Grid<Invoice> grid = new Grid<>(Invoice.class, false);

    public AccountsReceivableView(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
        addClassName("accounts-receivable-view");
        setSizeFull();

        configureGrid();
        add(createToolbar(), grid);
        updateList();
    }

    private HorizontalLayout createToolbar() {
        Button newInvoiceButton = new Button("New Invoice", click -> UI.getCurrent().navigate(InvoiceEditView.class));
        newInvoiceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(newInvoiceButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void configureGrid() {
        grid.addClassName("invoice-grid");
        grid.setSizeFull();
        grid.addColumn(Invoice::getInvoiceNumber).setHeader("Invoice Number").setSortable(true);
        grid.addColumn(invoice -> invoice.getProject() != null ? invoice.getProject().getName() : "").setHeader("Project").setSortable(true);
        grid.addColumn(Invoice::getAmount).setHeader("Amount").setSortable(true);
        grid.addColumn(Invoice::getInvoiceDate).setHeader("Invoice Date").setSortable(true);
        grid.addColumn(Invoice::getDueDate).setHeader("Due Date").setSortable(true);
        grid.addColumn(invoice -> invoice.getStatus().getDisplayName()).setHeader("Status").setSortable(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate("accounts-receivable/" + event.getValue().getId() + "/edit");
            }
        });
    }

    private void updateList() {
        grid.setItems(query -> invoiceService.list(PageRequest.of(query.getPage(), query.getPageSize())).stream());
    }
}
