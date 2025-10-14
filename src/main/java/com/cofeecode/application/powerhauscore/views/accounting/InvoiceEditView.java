package com.cofeecode.application.powerhauscore.views.accounting;

import com.cofeecode.application.powerhauscore.data.Currency;
import com.cofeecode.application.powerhauscore.data.Invoice;
import com.cofeecode.application.powerhauscore.data.InvoiceStatus;
import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.services.InvoiceService;
import com.cofeecode.application.powerhauscore.services.ProjectService;
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

@PageTitle("Edit Invoice")
@Route(value = "accounts-receivable/:invoiceID?/edit", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class InvoiceEditView extends Div implements BeforeEnterObserver {

    private final String INVOICE_ID = "invoiceID";
    private final InvoiceService invoiceService;
    private final ProjectService projectService;

    private Invoice invoice;
    private BeanValidationBinder<Invoice> binder;

    private TextField invoiceNumber = new TextField("Invoice Number");
    private ComboBox<Project> project = new ComboBox<>("Project");
    private TextField amount = new TextField("Amount");
    private ComboBox<Currency> currency = new ComboBox<>("Currency");
    private DatePicker invoiceDate = new DatePicker("Invoice Date");
    private DatePicker dueDate = new DatePicker("Due Date");
    private ComboBox<InvoiceStatus> status = new ComboBox<>("Status");

    private Button save = new Button("Save");
    private Button cancel = new Button("Cancel");
    private Button delete = new Button("Delete");

    public InvoiceEditView(InvoiceService invoiceService, ProjectService projectService) {
        this.invoiceService = invoiceService;
        this.projectService = projectService;
        addClassName("invoice-edit-view");

        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());

        configureBinder();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> invoiceId = event.getRouteParameters().get(INVOICE_ID).map(Long::parseLong);
        if (invoiceId.isPresent()) {
            Optional<Invoice> invoiceFromBackend = invoiceService.get(invoiceId.get());
            if (invoiceFromBackend.isPresent()) {
                invoice = invoiceFromBackend.get();
            } else {
                Notification.show("Invoice not found", 3000, Notification.Position.BOTTOM_START);
                navigateToListView();
                return;
            }
        } else {
            invoice = new Invoice();
        }
        populateForm();
    }

    private void configureBinder() {
        binder = new BeanValidationBinder<>(Invoice.class);
        binder.bindInstanceFields(this);
    }

    private H3 createTitle() {
        return new H3(invoice == null || invoice.getId() == null ? "New Invoice" : "Edit Invoice");
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        project.setItems(projectService.findAll());
        project.setItemLabelGenerator(Project::getName);
        currency.setItems(Currency.values());
        currency.setItemLabelGenerator(Currency::getDisplayName);
        status.setItems(InvoiceStatus.values());
        status.setItemLabelGenerator(InvoiceStatus::getDisplayName);
        formLayout.add(invoiceNumber, project, amount, currency, invoiceDate, dueDate, status);
        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        save.addClickListener(e -> saveInvoice());
        cancel.addClickListener(e -> navigateToListView());
        delete.addClickListener(e -> deleteInvoice());

        return new HorizontalLayout(save, cancel, delete);
    }

    private void populateForm() {
        binder.readBean(invoice);
    }

    private void saveInvoice() {
        try {
            binder.writeBean(invoice);
            if (invoice.getId() == null) {
                invoiceService.create(invoice);
                Notification.show("Invoice created successfully.", 3000, Notification.Position.BOTTOM_START);
            } else {
                invoiceService.update(invoice);
                Notification.show("Invoice updated successfully.", 3000, Notification.Position.BOTTOM_START);
            }
            navigateToListView();
        } catch (Exception e) {
            Notification.show("An error occurred while saving the invoice: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START);
        }
    }

    private void deleteInvoice() {
        if (invoice.getId() != null) {
            invoiceService.delete(invoice.getId());
            Notification.show("Invoice deleted successfully.", 3000, Notification.Position.BOTTOM_START);
            navigateToListView();
        }
    }

    private void navigateToListView() {
        UI.getCurrent().navigate(AccountsReceivableView.class);
    }
}
