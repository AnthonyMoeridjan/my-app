package com.cofeecode.application.powerhauscore.views.Projects;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;

@PageTitle("Projects")
@Route(value = "projects", layout = MainLayout.class)
@RolesAllowed({"USER", "RVC", "HR", "ADMIN"})
@Uses(Icon.class)
public class ProjectListView extends Div {

    private final ProjectService projectService;
    private final VerticalLayout projectContainer = new VerticalLayout();

    public ProjectListView(ProjectService projectService) {
        this.projectService = projectService;
        addClassName("project-list-view");

        configureLayout();
        loadProjects();
    }

    private void configureLayout() {
        setSizeFull();
        addClassName("projects-view");

        // "New Project" Button
        Button newProjectButton = new Button("New Project", e -> UI.getCurrent().navigate("projects/edit"));
        newProjectButton.addClassName(LumoUtility.Margin.Top.SMALL);
        newProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout header = new HorizontalLayout(newProjectButton);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        projectContainer.setWidthFull();
        projectContainer.addClassName("project-container");

        add(header, projectContainer);
    }

    private void loadProjects() {
        projectContainer.removeAll();
        List<Project> projects = projectService.findAll(); // Fetch projects

        FlexLayout projectGrid = new FlexLayout();
        projectGrid.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        projectGrid.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        projectGrid.setWidthFull();

        for (Project project : projects) {
            projectGrid.add(createProjectCard(project));
        }

        projectContainer.add(projectGrid);
    }

    private Div createProjectCard(Project project) {
        Div card = new Div();
        card.addClassName("project-card");

        // Title
        H3 title = new H3(project.getName());
        title.addClassName("project-title");

        // Description
        Paragraph description = new Paragraph(project.getDescription());
        description.addClassName("project-description");

        // Project Status & Budget
        Span status = new Span("Status: " + project.getStatus());
        status.addClassName("project-status");

        Span budget = new Span("Budget: $" + project.getBudget());
        budget.addClassName("project-budget");

        // Project Actions
        Button editButton = new Button("Edit", e -> UI.getCurrent().navigate("projects/" + project.getId() + "/edit"));
        editButton.addClassName("edit-button");

        VerticalLayout content = new VerticalLayout(title, description, status, budget, editButton);
        content.setPadding(false);
        content.setSpacing(true);

        card.add(content);
        card.addClickListener(e -> UI.getCurrent().navigate("projects/" + project.getId() + "/edit"));

        return card;
    }
}
