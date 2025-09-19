package com.cofeecode.application.powerhauscore.views.accounting;

import com.cofeecode.application.powerhauscore.data.Bill;
import com.cofeecode.application.powerhauscore.services.BillService;
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

@PageTitle("Accounts Payable")
@Route(value = "accounts-payable", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class AccountsPayableView extends VerticalLayout {

    private final BillService billService;

    private Grid<Bill> grid = new Grid<>(Bill.class, false);

    public AccountsPayableView(BillService billService) {
        this.billService = billService;
        addClassName("accounts-payable-view");
        setSizeFull();

        configureGrid();
        add(createToolbar(), grid);
        updateList();
    }

    private HorizontalLayout createToolbar() {
        Button newBillButton = new Button("New Bill", click -> UI.getCurrent().navigate(BillEditView.class));
        newBillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(newBillButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void configureGrid() {
        grid.addClassName("bill-grid");
        grid.setSizeFull();
        grid.addColumn(Bill::getBillNumber).setHeader("Bill Number").setSortable(true);
        grid.addColumn(bill -> bill.getProject() != null ? bill.getProject().getName() : "").setHeader("Project").setSortable(true);
        grid.addColumn(Bill::getAmount).setHeader("Amount").setSortable(true);
        grid.addColumn(Bill::getBillDate).setHeader("Bill Date").setSortable(true);
        grid.addColumn(Bill::getDueDate).setHeader("Due Date").setSortable(true);
        grid.addColumn(bill -> bill.getStatus().getDisplayName()).setHeader("Status").setSortable(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate("accounts-payable/" + event.getValue().getId() + "/edit");
            }
        });
    }

    private void updateList() {
        grid.setItems(query -> billService.list(PageRequest.of(query.getPage(), query.getPageSize())).stream());
    }
}
