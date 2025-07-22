package com.cofeecode.application.powerhauscore.views.project;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.ProjectStatus;
import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;

@PageTitle("Projects Dashboard")
@Route(value = "projects", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN", "HR"}) // Adjust roles as needed
public class ProjectDashboardView extends VerticalLayout {

    private final ProjectService projectService;

    private Grid<Project> grid = new Grid<>(Project.class, false);

    public ProjectDashboardView(ProjectService projectService) {
        this.projectService = projectService;
        addClassName("project-dashboard-view");
        setSizeFull();

        configureGrid();
        add(createToolbar(), grid);
        updateList();
    }

    private HorizontalLayout createToolbar() {
        Button newProjectButton = new Button("New Project", click -> UI.getCurrent().navigate(ProjectEditView.class));
        newProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(newProjectButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void configureGrid() {
        grid.addClassName("project-grid");
        grid.setSizeFull();
        grid.addColumn(Project::getName).setHeader("Name").setSortable(true);
        grid.addColumn(project -> project.getStatus() != null ? project.getStatus().getDisplayName() : "").setHeader("Status").setSortable(true);
        grid.addColumn(Project::getClient).setHeader("Client").setSortable(true);
        grid.addColumn(Project::getStartDate).setHeader("Start Date").setSortable(true);
        grid.addColumn(Project::getEndDate).setHeader("End Date").setSortable(true);
        grid.addColumn(Project::getManager).setHeader("Manager").setSortable(true);
        grid.addColumn(Project::getQuoteAmount).setHeader("Quote Amount").setSortable(true);
        grid.addColumn(Project::getInvoiceAmount).setHeader("Invoice Amount").setSortable(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate("projects/" + event.getValue().getId() + "/edit");
            }
        });
    }

    private void updateList() {
        // For now, load all projects. Pagination and filtering will be added later.
        grid.setItems(query -> projectService.list(PageRequest.of(query.getPage(), query.getPageSize())).stream());
    }
}
