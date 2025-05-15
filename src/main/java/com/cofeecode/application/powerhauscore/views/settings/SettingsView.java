package com.cofeecode.application.powerhauscore.views.settings;

import com.cofeecode.application.powerhauscore.services.SettingsService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Settings")
@Route(value = "settings", layout = MainLayout.class)
@RolesAllowed({"ADMIN"}) // Adjust based on your roles
public class SettingsView extends VerticalLayout {

    private TextField uploadPathField = new TextField("Upload File Directory");

    public SettingsView(SettingsService settingsService) {
        addClassName("settings-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Settings");
        add(title);

        uploadPathField.setValue(settingsService.getUploadDirectory()); // prefill from saved settings

        Button save = new Button("Save", event -> {
            settingsService.setUploadDirectory(uploadPathField.getValue()); // save path
            Notification.show("Settings saved", 3000, Notification.Position.TOP_CENTER);
        });


        add(accountSettingsSection());
        add(appearanceSettingsSection());
        add(placeholderSection("Notifications (Coming Soon)"));
        add(placeholderSection("Privacy & Security (Coming Soon)"));
    }

    private Component accountSettingsSection() {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP));

        TextField name = new TextField("Display Name");
        name.setValue("John Doe"); // Load real user info here

        EmailField email = new EmailField("Email");
        email.setValue("john.doe@example.com"); // Load real email

        PasswordField password = new PasswordField("Change Password");

        Button save = new Button("Save", event -> {
            // TODO: Save logic
            Notification.show("Account settings saved", 3000, Notification.Position.TOP_CENTER);
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        form.add(name, email, password);
        form.add(uploadPathField); // add this to the account settings section

        return new VerticalLayout(new H3("Account"), form, save);
    }

    private Component appearanceSettingsSection() {
        FormLayout form = new FormLayout();
        ComboBox<String> theme = new ComboBox<>("Theme");
        theme.setItems("System Default", "Light", "Dark");
        theme.setValue("System Default");

        form.add(theme);

        Button save = new Button("Apply", event -> {
            // TODO: Save user theme preference
            Notification.show("Appearance settings saved", 3000, Notification.Position.TOP_CENTER);
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        return new VerticalLayout(new H3("Appearance"), form, save);
    }

    private Component placeholderSection(String title) {
        return new VerticalLayout(new H3(title), new Paragraph("This section will be available in a future update."));
    }
}
