package com.cofeecode.application.powerhauscore.views.timesheet;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.Timesheet;
import com.cofeecode.application.powerhauscore.data.Worker;
import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.services.TimesheetService;
import com.cofeecode.application.powerhauscore.services.WorkerService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

@PageTitle("Edit Timesheet")
@Route(value = "timesheets/edit/:timesheetID?", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN", "HR"})
public class TimesheetEditView extends VerticalLayout implements BeforeEnterObserver {

    private final TimesheetService timesheetService;
    private final WorkerService workerService;
    private final ProjectService projectService;

    private final Binder<Timesheet> binder = new BeanValidationBinder<>(Timesheet.class);

    private final Select<Worker> workerSelect = new Select<>();
    private final Select<Project> projectSelect = new Select<>();
    private final DatePicker dateField = new DatePicker("Date");
    private final TimePicker startTimeField = new TimePicker("Start Time");
    private final TimePicker endTimeField = new TimePicker("End Time");
    private final TextArea description = new TextArea("Description");
    private final NumberField hoursWorked = new NumberField("Hours Worked");
    private final NumberField earningsField = new NumberField("Verdiend");

    private final Button saveButton = new Button("Save");
    private final Button deleteButton = new Button("Delete");
    private final Button cancelButton = new Button("Cancel", e -> UI.getCurrent().navigate(TimesheetListView.class));

    private Timesheet timesheet;

    public TimesheetEditView(TimesheetService timesheetService, WorkerService workerService, ProjectService projectService) {
        this.timesheetService = timesheetService;
        this.workerService = workerService;
        this.projectService = projectService;

        addClassName("timesheet-edit-view");
        setSizeFull();

        boolean isAdmin = hasRole("ADMIN");

        deleteButton.setEnabled(isAdmin);

        // Build the UI
        add(new H2("Edit Timesheet")); // Page heading
        add(createFormLayout());
        add(createButtonLayout());

        configureBinder();
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                // 1 column on small screens, 2 columns on medium+ screens
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        // Worker selection
        workerSelect.setLabel("Worker");
        workerSelect.setItems(workerService.findAll());
        workerSelect.setItemLabelGenerator(w -> w.getFirstName() + " " + w.getLastName());

        // Project selection
        projectSelect.setLabel("Project");
        projectSelect.setItems(projectService.findAll());
        projectSelect.setItemLabelGenerator(Project::getName);

        earningsField.setReadOnly(true);
        earningsField.setPrefixComponent(new Span("SRD"));

        HorizontalLayout earingsLayout = new HorizontalLayout();
        earingsLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        earingsLayout.add(hoursWorked, earningsField);

        // TimePickers
        startTimeField.setStep(Duration.ofMinutes(15));
        startTimeField.addValueChangeListener(event -> calculateHoursAndEarnings());
        endTimeField.setStep(Duration.ofMinutes(15));
        endTimeField.addValueChangeListener(event -> calculateHoursAndEarnings());

        // Description
        description.setPlaceholder("Enter additional notes...");
        description.setWidthFull();
        description.setMaxLength(500);

        // Let’s bind each field in two columns
        formLayout.add(workerSelect, projectSelect);
        formLayout.add(startTimeField, dateField);
        formLayout.add(endTimeField, earingsLayout); // hoursLabel in the next column
        formLayout.add(description, 2);      // Span 2 columns

        hoursWorked.getStyle().set("font-weight", "bold");

        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        // Themes for styling
        saveButton.getElement().setAttribute("theme", "primary");
        deleteButton.getElement().setAttribute("theme", "error");

        saveButton.addClickListener(e -> saveTimesheet());
        deleteButton.addClickListener(e -> deleteTimesheet());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, deleteButton, cancelButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        return buttonLayout;
    }

    private void configureBinder() {
        binder.bindInstanceFields(this);

        binder.forField(dateField)
                .withValidator(date -> date != null, "Date is required")
                .bind(Timesheet::getDate, Timesheet::setDate);

        binder.forField(startTimeField)
                .withValidator(time -> time != null, "Start time is required")
                .bind(Timesheet::getStartTime, Timesheet::setStartTime);

        binder.forField(endTimeField)
                .withValidator(time -> time != null, "End time is required")
                .bind(Timesheet::getEndTime, Timesheet::setEndTime);

        binder.forField(workerSelect)
                .withValidator(worker -> worker != null, "Worker is required")
                .bind(Timesheet::getWorker, Timesheet::setWorker);

        binder.forField(projectSelect)
                .withValidator(project -> project != null, "Project is required")
                .bind(Timesheet::getProject, Timesheet::setProject);
    }

    private void saveTimesheet() {
        try {
            binder.writeBean(timesheet);
            timesheetService.save(timesheet);
            Notification.show("Timesheet saved successfully.");
            UI.getCurrent().navigate(TimesheetListView.class);
        } catch (ObjectOptimisticLockingFailureException e) {
            Notification.show("Error: This timesheet was modified by another user.", 5000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void deleteTimesheet() {
        if (timesheet != null && timesheet.getId() != null) {
            timesheetService.delete(timesheet.getId());
            Notification.show("Timesheet deleted successfully.");
            UI.getCurrent().navigate(TimesheetListView.class);
        }
    }

    private double calculateHoursWorked() {
        LocalTime start = startTimeField.getValue();
        LocalTime end = endTimeField.getValue();
        if (start != null && end != null && !end.isBefore(start)) {
            double hours = Duration.between(start, end).toMinutes() / 60.0;
            return Math.round(hours * 100.0) / 100.0; // Round to 2 decimals
        }
        return 0;
    }

    private void calculateHoursAndEarnings() {
        LocalTime start = startTimeField.getValue();
        LocalTime end = endTimeField.getValue();

        if (start != null && end != null && !end.isBefore(start) && workerSelect.getValue() != null) {
            double hoursWorked = Duration.between(start, end).toMinutes() / 60.0;
            Worker selectedWorker = workerSelect.getValue();
            BigDecimal earnings = calculateEarnings(hoursWorked, selectedWorker.getUurloon());

            this.hoursWorked.setValue(hoursWorked);
            earningsField.setValue(earnings.doubleValue());
        }
    }

    private BigDecimal calculateEarnings(double hoursWorked, BigDecimal uurloon) {
        double normalHours = Math.min(hoursWorked, 9);  // First 9 hours
        double overtimeHours = Math.max(0, hoursWorked - 9); // Overtime hours

        BigDecimal normalPay = uurloon.multiply(BigDecimal.valueOf(normalHours));
        BigDecimal overtimePay = uurloon.multiply(BigDecimal.valueOf(overtimeHours)).multiply(BigDecimal.valueOf(1.5));

        return normalPay.add(overtimePay);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> timesheetID = event.getRouteParameters().get("timesheetID");
        if (timesheetID.isPresent()) {
            Optional<Timesheet> timesheetFromDB = timesheetService.findById(Long.parseLong(timesheetID.get()));
            if (timesheetFromDB.isPresent()) {
                timesheet = timesheetFromDB.get();
                binder.readBean(timesheet);
                calculateHoursAndEarnings();
            } else {
                Notification.show("Timesheet not found.", 3000, Notification.Position.BOTTOM_START);
                UI.getCurrent().navigate(TimesheetListView.class);
            }
        } else {
            timesheet = new Timesheet();
        }
    }
    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_" + role));
    }
}
