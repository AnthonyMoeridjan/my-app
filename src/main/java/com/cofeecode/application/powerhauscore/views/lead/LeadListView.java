package com.cofeecode.application.powerhauscore.views.lead;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.ProjectStatus;
import com.cofeecode.application.powerhauscore.services.ProjectService;
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

import java.util.List;

@PageTitle("Leads")
@Route(value = "leads", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN", "HR"})
public class LeadListView extends VerticalLayout {

    private final ProjectService projectService;
    private final Grid<Project> grid = new Grid<>(Project.class, false);

    public LeadListView(ProjectService projectService) {
        this.projectService = projectService;
        addClassName("lead-list-view");
        setSizeFull();

        configureGrid();
        add(createToolbar(), grid);
        updateList();
    }

    private HorizontalLayout createToolbar() {
        Button newLeadButton = new Button("New Lead", click -> UI.getCurrent().navigate(LeadEditView.class));
        newLeadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(newLeadButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private void configureGrid() {
        grid.addClassName("lead-grid");
        grid.setSizeFull();
        grid.addColumn(Project::getName).setHeader("Name").setSortable(true);
        grid.addColumn(Project::getClient).setHeader("Client").setSortable(true);
        grid.addColumn(Project::getManager).setHeader("Owner").setSortable(true);

        grid.addComponentColumn(project -> {
            Button convert = new Button("Convert", e -> convertLead(project));
            convert.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            return convert;
        }).setHeader("Actions");

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(LeadEditView.class + "/" + event.getValue().getId());
            }
        });
    }

    private void updateList() {
        List<Project> leads = projectService.findByStatus(ProjectStatus.LEAD);
        grid.setItems(leads);
    }

    private void convertLead(Project lead) {
        projectService.updateStatus(lead.getId(), ProjectStatus.APPROVED);
        UI.getCurrent().navigate("projects/" + lead.getId() + "/edit");
    }
}
