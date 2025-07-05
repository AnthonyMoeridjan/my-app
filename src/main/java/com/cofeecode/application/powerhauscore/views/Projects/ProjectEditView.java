package com.cofeecode.application.powerhauscore.views.Projects;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@PageTitle("Edit Project")
//@Route(value = "projects/:projectID?/edit", layout = MainLayout.class)
@RolesAllowed({"USER","RVC", "HR", "ADMIN"})
public class ProjectEditView extends Div implements BeforeEnterObserver {

    private final String PROJECT_ID = "projectID";
    private final ProjectService projectService;

    private Project project;

    // Form fields
    private final com.vaadin.flow.component.textfield.TextField name = new com.vaadin.flow.component.textfield.TextField("Project Name");
    private final com.vaadin.flow.component.textfield.TextField description = new com.vaadin.flow.component.textfield.TextField("Description");
    private final DatePicker startDate = new DatePicker("Start Date");
    private final DatePicker endDate = new DatePicker("End Date");
    private final com.vaadin.flow.component.textfield.TextField budget = new com.vaadin.flow.component.textfield.TextField("Budget");
    private final com.vaadin.flow.component.textfield.TextField status = new com.vaadin.flow.component.textfield.TextField("Status");
    private final com.vaadin.flow.component.textfield.TextField client = new com.vaadin.flow.component.textfield.TextField("Client");
    private final com.vaadin.flow.component.textfield.TextField manager = new com.vaadin.flow.component.textfield.TextField("Manager");
    private final com.vaadin.flow.component.textfield.TextField location = new com.vaadin.flow.component.textfield.TextField("Location");
    private final com.vaadin.flow.component.checkbox.Checkbox isPriority = new com.vaadin.flow.component.checkbox.Checkbox("Priority");

    private final Button save = new Button("Save");
    private final Button cancel = new Button("Cancel");
    private final Button delete = new Button("Delete");

    private BeanValidationBinder<Project> binder;

    public ProjectEditView(ProjectService projectService) {
        this.projectService = projectService;
        addClassName("edit-view");

        // Check if the user has ADMIN access
        boolean isAdmin = hasRole("ADMIN");

        // Disable delete button for non-admins
        delete.setEnabled(isAdmin);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        delete.addClickListener(e -> {
            if (!isAdmin) {
                Notification.show("You do not have permission to delete projects!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            deleteProject();
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check if we have a :projectID parameter
        Optional<Long> projectId = event.getRouteParameters().get(PROJECT_ID).map(Long::parseLong);

        if (projectId.isPresent()) {
            // Existing project
            Optional<Project> projectFromBackend = projectService.get(projectId.get());
            if (projectFromBackend.isPresent()) {
                this.project = projectFromBackend.get();
            } else {
                // Not found, redirect back to list
                Notification.show("Project not found", 3000, Notification.Position.BOTTOM_START);
                event.forwardTo(ProjectListView.class);
                return;
            }
        } else {
            // No project ID => new project
            this.project = new Project();
        }

        createFormLayout();
        populateForm(this.project);
    }

    private void createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(name, description, startDate, endDate, budget, status, client, manager, location, isPriority);

        // Buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        save.addClickListener(e -> saveProject());
        cancel.addClickListener(e -> UI.getCurrent().navigate(ProjectListView.class));
        delete.addClickListener(e -> deleteProject());
        delete.setVisible(this.project.getId() != null); // Show delete only if editing existing

        buttonLayout.add(save, cancel, delete);

        // Build binder
        binder = new BeanValidationBinder<>(Project.class);

        // Example simple binding
        binder.bindInstanceFields(this);

        add(formLayout, buttonLayout);
    }

    private void populateForm(Project project) {
        // Let the binder load the Project data
        binder.readBean(project);

        // If new project, set default values if desired
        if (project.getId() == null) {
            startDate.setValue(LocalDate.now());
        }
    }

    private void saveProject() {
        try {
            binder.writeBean(this.project);

            // Convert budget from TextField => BigDecimal, for example
            try {
                BigDecimal budgetVal = new BigDecimal(budget.getValue());
                this.project.setBudget(budgetVal);
            } catch (NumberFormatException e) {
                // If needed, you can show validation error
                this.project.setBudget(BigDecimal.ZERO);
            }

            if (this.project.getId() == null) {
                // Create new
                projectService.create(this.project);
                Notification.show("Project created");
            } else {
                // Update existing
                projectService.update(this.project);
                Notification.show("Project updated");
            }

            UI.getCurrent().navigate(ProjectListView.class);

        } catch (ValidationException e) {
            Notification.show("Validation error: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (ObjectOptimisticLockingFailureException e) {
            Notification.show("Conflict: Project was modified by another user.", 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void deleteProject() {
        if (this.project != null && this.project.getId() != null) {
            projectService.delete(this.project.getId());
            Notification.show("Project deleted");
            UI.getCurrent().navigate(ProjectListView.class);
        }
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_" + role));
    }

}
