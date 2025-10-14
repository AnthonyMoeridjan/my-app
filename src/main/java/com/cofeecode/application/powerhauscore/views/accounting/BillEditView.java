package com.cofeecode.application.powerhauscore.views.accounting;

import com.cofeecode.application.powerhauscore.data.*;
import com.cofeecode.application.powerhauscore.services.BillService;
import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.services.TransactionService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

@PageTitle("Edit Bill")
@Route(value = "accounts-payable/:billID?/edit", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class BillEditView extends Div implements BeforeEnterObserver {

    private final String BILL_ID = "billID";
    private final BillService billService;
    private final ProjectService projectService;
    private final TransactionService transactionService;

    private Bill bill;
    private BeanValidationBinder<Bill> binder;

    private TextField billNumber = new TextField("Bill Number");
    private ComboBox<Project> project = new ComboBox<>("Project");
    private TextField amount = new TextField("Amount");
    private ComboBox<Currency> currency = new ComboBox<>("Currency");
    private DatePicker billDate = new DatePicker("Bill Date");
    private DatePicker dueDate = new DatePicker("Due Date");
    private ComboBox<BillStatus> status = new ComboBox<>("Status");
    private ComboBox<Transaction> transaction = new ComboBox<>("Transaction");

    private Button save = new Button("Save");
    private Button cancel = new Button("Cancel");
    private Button delete = new Button("Delete");

    public BillEditView(BillService billService, ProjectService projectService, TransactionService transactionService) {
        this.billService = billService;
        this.projectService = projectService;
        this.transactionService = transactionService;
        addClassName("bill-edit-view");

        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());

        configureBinder();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> billId = event.getRouteParameters().get(BILL_ID).map(Long::parseLong);
        if (billId.isPresent()) {
            Optional<Bill> billFromBackend = billService.get(billId.get());
            if (billFromBackend.isPresent()) {
                bill = billFromBackend.get();
            } else {
                Notification.show("Bill not found", 3000, Notification.Position.BOTTOM_START);
                navigateToListView();
                return;
            }
        } else {
            bill = new Bill();
        }
        populateForm();
    }

    private void configureBinder() {
        binder = new BeanValidationBinder<>(Bill.class);
        binder.bindInstanceFields(this);
    }

    private H3 createTitle() {
        return new H3(bill == null || bill.getId() == null ? "New Bill" : "Edit Bill");
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        project.setItems(projectService.findAll());
        project.setItemLabelGenerator(Project::getName);
        currency.setItems(Currency.values());
        currency.setItemLabelGenerator(Currency::getDisplayName);
        status.setItems(BillStatus.values());
        status.setItemLabelGenerator(BillStatus::getDisplayName);
        transaction.setItems(transactionService.findAll());
        transaction.setItemLabelGenerator(Transaction::getDescription);
        formLayout.add(billNumber, project, amount, currency, billDate, dueDate, status, transaction);
        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        save.addClickListener(e -> saveBill());
        cancel.addClickListener(e -> navigateToListView());
        delete.addClickListener(e -> deleteBill());

        return new HorizontalLayout(save, cancel, delete);
    }

    private void populateForm() {
        binder.readBean(bill);
    }

    private void saveBill() {
        try {
            binder.writeBean(bill);
            if (bill.getId() == null) {
                billService.create(bill);
                Notification.show("Bill created successfully.", 3000, Notification.Position.BOTTOM_START);
            } else {
                billService.update(bill);
                Notification.show("Bill updated successfully.", 3000, Notification.Position.BOTTOM_START);
            }
            navigateToListView();
        } catch (Exception e) {
            Notification.show("An error occurred while saving the bill: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START);
        }
    }

    private void deleteBill() {
        if (bill.getId() != null) {
            billService.delete(bill.getId());
            Notification.show("Bill deleted successfully.", 3000, Notification.Position.BOTTOM_START);
            navigateToListView();
        }
    }

    private void navigateToListView() {
        UI.getCurrent().navigate(AccountsPayableView.class);
    }
}
