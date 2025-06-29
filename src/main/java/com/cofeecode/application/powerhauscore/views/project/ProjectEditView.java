package com.cofeecode.application.powerhauscore.views.project;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.ProjectStatus;
import com.cofeecode.application.powerhauscore.customfield.PriceField; // Assuming similar PriceField can be used for budget
import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.cofeecode.application.powerhauscore.security.AuthenticatedUser; // For role-based access
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

@PageTitle("Edit Project")
@Route(value = "projects/:projectID?/edit", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN", "HR"}) // Adjust roles as needed
public class ProjectEditView extends Div implements BeforeEnterObserver {

    private final String PROJECT_ID = "projectID";
    private final ProjectService projectService;
    private final AuthenticatedUser authenticatedUser;

    private Project project;
    private BeanValidationBinder<Project> binder;

    // Form fields
    private TextField name = new TextField("Project Name");
    private TextArea description = new TextArea("Description");
    private DatePicker startDate = new DatePicker("Start Date");
    private DatePicker endDate = new DatePicker("End Date");
    private PriceField budget = new PriceField("Budget"); // Using PriceField, ensure it's suitable or create a NumberField
    private ComboBox<ProjectStatus> status = new ComboBox<>("Status");
    private TextField client = new TextField("Client");
    private TextField manager = new TextField("Manager");
    private TextField location = new TextField("Location");
    private Checkbox isPriority = new Checkbox("Is Priority?");

    private Button save = new Button("Save");
    private Button cancel = new Button("Cancel");
    private Button delete = new Button("Delete");

    private boolean isEditMode = false; // To manage read-only state, similar to TransactionEditView

    public ProjectEditView(ProjectService projectService, AuthenticatedUser authenticatedUser) {
        this.projectService = projectService;
        this.authenticatedUser = authenticatedUser;
        addClassName("project-edit-view");

        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());

        configureBinder();
        configureAccessControls(); // For save/delete buttons based on roles
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> projectId = event.getRouteParameters().get(PROJECT_ID).map(Long::parseLong);
        if (projectId.isPresent()) {
            Optional<Project> projectFromBackend = projectService.get(projectId.get());
            if (projectFromBackend.isPresent()) {
                project = projectFromBackend.get();
                isEditMode = false; // Start in view mode for existing projects
            } else {
                Notification.show("Project not found", 3000, Notification.Position.BOTTOM_START);
                navigateToDashboard();
                return;
            }
        } else {
            project = new Project();
            isEditMode = true; // New projects start in edit mode
        }
        populateForm();
        updatePageTitle();
        updateButtonStates();
        setFieldsReadOnly(!isEditMode);
    }

    private void configureBinder() {
        binder = new BeanValidationBinder<>(Project.class);

        // Bind fields. For PriceField, a converter might be needed if it doesn't directly support BigDecimal.
        // Assuming PriceField handles its currency and amount binding internally or we adapt it.
        // For simplicity, let's assume direct binding or a simple converter for budget.
        binder.forField(budget)
                .withConverter(
                        price -> price != null ? price.getAmountBigDecimal() : null, // Method to get BigDecimal from PriceField
                        bigDecimal -> bigDecimal != null ? new PriceField.Price(bigDecimal, budget.getCurrency().getValue()) : null // Method to set PriceField from BigDecimal
                ).bind(Project::getBudget, Project::setBudget);

        budget.getCurrency().addValueChangeListener(event -> {
            if (project != null && project.getBudget() != null) {
                 // If you need to store currency with project, handle it here.
                 // project.setCurrency(event.getValue()); // Example if Project entity has currency field
            }
        });


        status.setItems(ProjectStatus.values());
        status.setItemLabelGenerator(ProjectStatus::getDisplayName);

        binder.bindInstanceFields(this);
    }

    private void configureAccessControls() {
        // Example: Only ADMIN can delete
        boolean isAdmin = authenticatedUser.get().map(user -> user.getRoles().stream()
                .anyMatch(role -> role == com.cofeecode.application.powerhauscore.data.Role.ADMIN))
                .orElse(false);
        delete.setVisible(isAdmin);

        // Example: USER can edit, but maybe not create certain fields or only ADMIN can save critical changes
        // This can be more granular if needed. For now, save is generally available if user has a role.
        save.setEnabled(authenticatedUser.get().isPresent());
    }

    private H3 createTitle() {
        return new H3(project == null || project.getId() == null ? "New Project" : "Edit Project");
    }

    private void updatePageTitle() {
        if (project != null && project.getId() != null) {
            UI.getCurrent().getPage().setTitle("Edit Project - " + project.getName());
        } else {
            UI.getCurrent().getPage().setTitle("New Project");
        }
    }


    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(name, description, startDate, endDate, budget, status, client, manager, location, isPriority);
        // Adjust column span for wider fields like description
        formLayout.setColspan(description, 2);
        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        save.addClickListener(e -> saveProject());
        cancel.addClickListener(e -> navigateToDashboard());
        delete.addClickListener(e -> deleteProject());

        return new HorizontalLayout(save, cancel, delete);
    }

    private void updateButtonStates() {
        boolean canEdit = authenticatedUser.get().map(user -> user.getRoles().stream()
                        .anyMatch(role -> role == com.cofeecode.application.powerhauscore.data.Role.ADMIN ||
                                role == com.cofeecode.application.powerhauscore.data.Role.HR ||
                                role == com.cofeecode.application.powerhauscore.data.Role.USER )) // Assuming USER can edit
                .orElse(false);

        save.setVisible(isEditMode && canEdit);
        // Delete button visibility is based on admin role and if the project exists
        boolean isAdmin = authenticatedUser.get().map(user -> user.getRoles().stream()
                        .anyMatch(role -> role == com.cofeecode.application.powerhauscore.data.Role.ADMIN))
                .orElse(false);
        delete.setVisible(project.getId() != null && isAdmin && !isEditMode); // Show delete only in view mode for existing items by admin

        // "Edit" button to switch to edit mode (if we add one, similar to TransactionEditView)
        // For now, new projects are directly in edit mode, existing ones load in view mode.
        // We might add an explicit "Edit" button later.
    }


    private void setFieldsReadOnly(boolean readOnly) {
        name.setReadOnly(readOnly);
        description.setReadOnly(readOnly);
        startDate.setReadOnly(readOnly);
        endDate.setReadOnly(readOnly);
        budget.setReadOnly(readOnly); // PriceField needs to correctly implement setReadOnly
        status.setReadOnly(readOnly);
        client.setReadOnly(readOnly);
        manager.setReadOnly(readOnly);
        location.setReadOnly(readOnly);
        isPriority.setReadOnly(readOnly);
    }


    private void populateForm() {
        if (project.getId() == null) { // New project
            binder.readBean(null); // Clear form
            status.setValue(ProjectStatus.LEAD); // Default status
            budget.clear(); // Clear PriceField
        } else { // Existing project
            binder.readBean(project);
            if (project.getBudget() != null) {
                // Assuming PriceField has a method to set value from BigDecimal and currency
                // This part depends on PriceField's API.
                // For example: budget.setValue(project.getBudget(), project.getCurrency());
                // If PriceField is simple, its binder converter should handle this.
            }
        }
    }

    private void saveProject() {
        try {
            binder.writeBean(project);
            if (project.getId() == null) {
                projectService.create(project);
                Notification.show("Project created successfully.", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                projectService.update(project);
                Notification.show("Project updated successfully.", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            navigateToDashboard();
        } catch (ValidationException e) {
            Notification.show("Validation error: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (ObjectOptimisticLockingFailureException e) {
            Notification.show("Conflict: Project was modified by another user. Please refresh and try again.", 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("An error occurred while saving: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteProject() {
        if (project.getId() != null) {
            try {
                projectService.delete(project.getId());
                Notification.show("Project deleted successfully.", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                navigateToDashboard();
            } catch (Exception e) {
                Notification.show("Error deleting project: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }

    private void navigateToDashboard() {
        UI.getCurrent().navigate(ProjectDashboardView.class);
    }
}
