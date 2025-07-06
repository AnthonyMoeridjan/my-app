package com.cofeecode.application.powerhauscore.views.workers;
import com.cofeecode.application.powerhauscore.data.Worker;
import com.cofeecode.application.powerhauscore.services.WorkerService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox; // if you need any checkboxes
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker; // if you need date pickers
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

@PageTitle("Worker Management")
@Route(value = "workers/:workerID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed({"USER", "HR", "ADMIN"})
@Uses(Icon.class)
public class WorkerMasterDetailView extends Div implements BeforeEnterObserver {

    // Route parameter keys
    private static final String WORKER_ID = "workerID";
    private static final String WORKER_EDIT_ROUTE_TEMPLATE = "workers/%s/edit";

    private final Grid<Worker> grid = new Grid<>(Worker.class, false);

    private ComboBox<String> filterStatus;

    // Form fields
    private TextField firstName;
    private TextField lastName;
    private TextField phoneNumber;
    private TextField email;
    private TextField address;
    private TextField bankName;
    private TextField bankAccountNumber;
    private TextField role;
    private BigDecimalField uurloon;
    private Checkbox activeCheckbox;

    // Action buttons
    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");

    // Inject the access checker to check user roles
    private final AccessAnnotationChecker accessChecker = new AccessAnnotationChecker();

    // Binder
    private final BeanValidationBinder<Worker> binder = new BeanValidationBinder<>(Worker.class);

    private Worker worker; // The currently selected Worker in the form

    private final WorkerService workerService;

    public WorkerMasterDetailView(WorkerService workerService) {
        this.workerService = workerService;
        addClassNames("master-detail-view");
        setSizeFull();

        // Initialize filterStatus first
        filterStatus = new ComboBox<>("Filter Workers");
        filterStatus.setItems("Show All", "Active", "Inactive");
        filterStatus.setValue("Active"); // Default selection
        filterStatus.addValueChangeListener(e -> refreshGrid());

        // Create UI with a split layout: grid on the left, form on the right
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        createGridLayout(splitLayout); // This will add filterStatus and grid to the layout
        createEditorLayout(splitLayout);
        add(splitLayout);

        // Configure Grid columns
        grid.addColumn(Worker::getFirstName).setHeader("First Name").setAutoWidth(true);
        grid.addColumn(Worker::getLastName).setHeader("Last Name").setAutoWidth(true);
        grid.addColumn(Worker::getPhoneNumber).setHeader("Phone").setAutoWidth(true);
        grid.addColumn(Worker::getRole).setHeader("Role").setAutoWidth(true);
        grid.addColumn(Worker::getBankName).setHeader("Bank").setAutoWidth(true);
        grid.addColumn(Worker::getUurloon).setHeader("Uur Loon").setAutoWidth(true);
        grid.addColumn(worker -> worker.isActive() ? "Active" : "Inactive")
                .setHeader("Status")
                .setAutoWidth(true);

        // Initial data load is handled by refreshGrid() called after filter initialization
        refreshGrid();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // Click on a row to edit that Worker
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                // Navigate to /workers/<id>/edit
                UI.getCurrent().navigate(String.format(WORKER_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(WorkerMasterDetailView.class);
            }
        });

        binder.forField(uurloon)
                .withValidator(value -> value == null || value.compareTo(BigDecimal.ZERO) >= 0, "Amount cannot be negative")
                .bind(Worker::getUurloon, Worker::setUurloon);
        binder.forField(activeCheckbox).bind(Worker::isActive, Worker::setActive);

        // Configure the binder (auto-bind fields by matching their names to Worker)
        binder.bindInstanceFields(this);

        // Cancel -> clear the form and refresh
        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        // Save -> create/update the worker
        save.addClickListener(e -> {
            try {
                if (this.worker == null) {
                    this.worker = new Worker();
                }
                binder.writeBean(this.worker); // copy field values from form to worker
                if (this.worker.getId() == null) {
                    // new Worker
                    workerService.create(this.worker);
                } else {
                    // existing Worker
                    workerService.update(this.worker);
                }
                clearForm();
                refreshGrid();
                Notification.show("Worker record saved");
                UI.getCurrent().navigate(WorkerMasterDetailView.class);

            } catch (ObjectOptimisticLockingFailureException ex) {
                Notification n = Notification.show(
                        "Error updating. Someone else may have changed the record."
                );
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);

            } catch (ValidationException validationException) {
                Notification.show("Failed to save data. Check all required fields.");
            }
        });

        // Check if the user has ADMIN access before enabling the delete button
        boolean isAdmin = hasRole("ADMIN"); // Check if user has the ADMIN role

        // Disable delete button if the user is not an admin
        delete.setEnabled(isAdmin);

        // Delete action (only if admin)
        delete.addClickListener(e -> {
            if (!isAdmin) {
                Notification.show("You do not have permission to delete workers!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (worker != null && worker.getId() != null) {
                workerService.delete(worker.getId());
                clearForm();
                refreshGrid();
                Notification.show("Worker deleted");
                UI.getCurrent().navigate(WorkerMasterDetailView.class);
            }
        });
    }

    /**
     * Check if we have a route parameter (worker ID). If so, fetch that Worker from the DB.
     * If found, populate form. Otherwise, show a notification and refresh the grid.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> workerId = event.getRouteParameters().get(WORKER_ID).map(Long::parseLong);
        if (workerId.isPresent()) {
            Optional<Worker> workerFromBackend = workerService.get(workerId.get());
            if (workerFromBackend.isPresent()) {
                populateForm(workerFromBackend.get());
            } else {
                Notification.show(String.format("Requested worker not found (ID=%s)", workerId.get()),
                        3000, Notification.Position.BOTTOM_START);
                refreshGrid();
                event.forwardTo(WorkerMasterDetailView.class);
            }
        }
    }

    /**
     * Build the form layout for editing/creating a worker record
     */
    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        phoneNumber = new TextField("Phone");
        email = new TextField("Email");
        address = new TextField("Address");
        bankName = new TextField("Bank Name");
        bankAccountNumber = new TextField("Bank Account #");
        role = new TextField("Role");
        uurloon = new BigDecimalField("Uur Loon");
        uurloon.setPrefixComponent(new Span("SRD"));
        activeCheckbox = new Checkbox("Active Worker");

        formLayout.add(
                firstName, lastName, phoneNumber, email,
                address, bankName, bankAccountNumber, role, uurloon, activeCheckbox
        );

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        // Put this editor side on the right
        splitLayout.addToSecondary(editorLayoutDiv);
    }

    /**
     * Footer button layout with Save/Cancel
     */
    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");

        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        buttonLayout.add(save, delete, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    /**
     * Grid side of the SplitLayout
     */
    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");

        // Ensure the filter and grid are stacked vertically
        VerticalLayout layout = new VerticalLayout(filterStatus, grid);
        layout.setSizeFull(); // Ensure it takes up available space
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.expand(grid);

        grid.setSizeFull(); // Ensure the grid fills the space
        wrapper.setSizeFull();
        wrapper.add(layout);

        // Add the wrapper to the primary section
        splitLayout.addToPrimary(wrapper);
    }

    private void refreshGrid() {
        // Clear selection & refresh
        grid.select(null);
        if ("Inactive".equals(filterStatus.getValue())) {
            grid.setItems(query ->
                    workerService.listInactive(PageRequest.of(
                            query.getPage(),
                            query.getPageSize(),
                            VaadinSpringDataHelpers.toSpringDataSort(query)
                    )).stream()
            );
        } else if ("Show All".equals(filterStatus.getValue())) {
            grid.setItems(query ->
                    workerService.listAll(PageRequest.of(
                            query.getPage(),
                            query.getPageSize(),
                            VaadinSpringDataHelpers.toSpringDataSort(query)
                    )).stream()
            );
        } else { // Default to Active
            grid.setItems(query ->
                    workerService.listActive(PageRequest.of(
                            query.getPage(),
                            query.getPageSize(),
                            VaadinSpringDataHelpers.toSpringDataSort(query)
                    )).stream()
            );
        }
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Worker value) {
        this.worker = value;
        binder.readBean(this.worker);
    }
    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_" + role));
    }
}