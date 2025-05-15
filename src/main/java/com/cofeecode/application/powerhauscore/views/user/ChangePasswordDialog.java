package com.cofeecode.application.powerhauscore.views.user;

import com.cofeecode.application.powerhauscore.security.AuthenticatedUser;
import com.cofeecode.application.powerhauscore.services.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class ChangePasswordDialog extends Dialog {

    private final UserService userService;
    private final AuthenticatedUser authenticatedUser;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private PasswordField currentPassword = new PasswordField("Current Password");
    private PasswordField newPassword = new PasswordField("New Password");
    private PasswordField confirmPassword = new PasswordField("Confirm New Password");
    private Button saveButton = new Button("Save");
    private Button cancelButton = new Button("Cancel");

    public ChangePasswordDialog(AuthenticatedUser authenticatedUser, UserService userService) {
        this.authenticatedUser = authenticatedUser;
        this.userService = userService;

        setModal(true);
        setDraggable(false);
        setResizable(false);
        setWidth("400px"); // Professional fixed width
        getElement().getStyle().set("border-radius", "12px"); // Smooth corners

        // Style fields
        currentPassword.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        newPassword.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        confirmPassword.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        currentPassword.setRevealButtonVisible(true);
        newPassword.setRevealButtonVisible(true);
        confirmPassword.setRevealButtonVisible(true);

        saveButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);

        saveButton.setWidthFull();
        cancelButton.setWidthFull();

        saveButton.addClickListener(event -> changePassword());
        cancelButton.addClickListener(event -> close());

        // Layout
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(true);
        layout.addClassNames(LumoUtility.Padding.LARGE);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Title
        H2 title = new H2("Change Password");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);

        // Button layout
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

// Set button styles
        saveButton.setWidth("120px"); // Fixed width for consistency
        cancelButton.setWidth("120px");

// Add buttons to layout
        buttonLayout.add(cancelButton, saveButton);

        layout.add(title, currentPassword, newPassword, confirmPassword, buttonLayout);
        add(layout);
    }

    private void changePassword() {
        authenticatedUser.get().ifPresentOrElse(user -> {
            if (!passwordEncoder.matches(currentPassword.getValue(), user.getHashedPassword())) {
                Notification notification = Notification.show("Current password is incorrect", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (!newPassword.getValue().equals(confirmPassword.getValue())) {
                Notification.show("New passwords do not match", 3000, Notification.Position.MIDDLE);
                return;
            }

            user.setHashedPassword(passwordEncoder.encode(newPassword.getValue()));
            userService.update(user);
            Notification notification = Notification.show("Password changed successfully", 3000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        }, () -> Notification.show("User not found", 3000, Notification.Position.MIDDLE));
    }
}
