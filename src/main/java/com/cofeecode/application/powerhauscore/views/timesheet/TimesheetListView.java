package com.cofeecode.application.powerhauscore.views.timesheet;

import com.cofeecode.application.powerhauscore.data.Timesheet;
import com.cofeecode.application.powerhauscore.services.TimesheetService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@PageTitle("Timesheets")
@Route(value = "timesheets", layout = MainLayout.class)
@RolesAllowed({"USER","ADMIN", "HR"})
public class TimesheetListView extends VerticalLayout {

    private static final String TIMESHEET_EDIT_ROUTE_TEMPLATE = "timesheets/edit/%s";

    private final TimesheetService timesheetService;
    private final Grid<Timesheet> grid = new Grid<>(Timesheet.class, false);

    public TimesheetListView(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
        addClassName("timesheet-view");
        setSizeFull();

        configureGrid();
        add(new Button("New Timesheet", e -> UI.getCurrent().navigate("timesheets/edit")), grid);
        updateList();
    }

    private void configureGrid() {
        grid.addColumn(timesheet -> timesheet.getWorker().getFirstName() + " " + timesheet.getWorker().getLastName()).setHeader("Worker");
        grid.addColumn(timesheet -> timesheet.getProject().getName()).setHeader("Project");
        grid.addColumn(Timesheet::getDate).setHeader("Date");
        grid.addColumn(Timesheet::getHoursWorked).setHeader("Hours Worked");
        grid.addColumn(Timesheet::getEarnings).setHeader("Earned");

//        grid.addComponentColumn(timesheet -> new Button("Edit", e ->
//                        UI.getCurrent().navigate("timesheets/edit/" + timesheet.getId())))
//                .setHeader("Actions");
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(
                        String.format(TIMESHEET_EDIT_ROUTE_TEMPLATE, event.getValue().getId())
                );
            }
        });
    }

    private void updateList() {
        List<Timesheet> timesheets = timesheetService.findAll();
        grid.setItems(timesheets);
    }
}
