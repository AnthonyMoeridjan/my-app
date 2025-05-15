package com.cofeecode.application.powerhauscore.views.user;

import com.cofeecode.application.powerhauscore.data.Role;
import com.cofeecode.application.powerhauscore.data.User;
import com.cofeecode.application.powerhauscore.services.UserService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@PageTitle("Edit User")
@Route(value = "users/edit/:userID?", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
public class UserEditView extends VerticalLayout implements BeforeEnterObserver, BeforeLeaveObserver  {

    private final UserService userService;
    private final Binder<User> binder = new Binder<>(User.class);
    private final TextField username = new TextField("Username");
    private final TextField name = new TextField("Full Name");
    private final PasswordField password = new PasswordField("Password");
    private final MultiSelectComboBox<Role> roles = new MultiSelectComboBox<>("Roles");
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Image profilePicture = new Image();
    private final Upload upload;
    private byte[] profileImageData;

    private User user;
    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");
    private final Button cancel = new Button("Cancel", click -> UI.getCurrent().navigate(UserListView.class));

    public UserEditView(UserService userService) {
        this.userService = userService;
        addClassName("user-edit-view");
        setSizeFull();

        roles.setItems(Role.values());

        MemoryBuffer buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.addSucceededListener(event -> {
            try {
                profileImageData = buffer.getInputStream().readAllBytes();
                profilePicture.setSrc("data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(profileImageData));
            } catch (IOException e) {
                Notification.show("Failed to upload image");
            }
        });

        save.addClickListener(e -> saveUser());
        delete.addClickListener(e -> deleteUser());

        binder.bindInstanceFields(this);

        HorizontalLayout actions = new HorizontalLayout(save, delete, cancel);
        add(username, name, password, roles, upload, profilePicture, actions);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> userId = event.getRouteParameters().get("userID");
        if (userId.isPresent()) {
            Optional<User> userFromDb = userService.get(Long.parseLong(userId.get()));
            if (userFromDb.isPresent()) {
                user = userFromDb.get();
                binder.readBean(user);
                if (user.getProfilePicture() != null) {
                    profilePicture.setSrc("data:image/jpeg;base64," +
                            java.util.Base64.getEncoder().encodeToString(user.getProfilePicture()));
                }
            }
        } else {
            user = new User(); // If no ID is provided, assume creating a new user
        }
    }
    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        if (binder.hasChanges()) {
            Notification.show("You have unsaved changes!");
        }
    }

    private void saveUser() {
        try {
            binder.writeBean(user);

            // Hash the password only if the user is setting a new one
            if (!password.isEmpty()) {
                user.setHashedPassword(passwordEncoder.encode(password.getValue())); // Hash before saving
            }

            // Save profile picture if uploaded
            if (profileImageData != null) {
                user.setProfilePicture(profileImageData);
            }

            userService.update(user);
            Notification.show("User saved successfully");
            UI.getCurrent().navigate(UserListView.class);
        } catch (Exception e) {
            Notification.show("Error saving user: " + e.getMessage());
        }
    }

    private void deleteUser() {
        if (user != null) {
            userService.delete(user.getId());
            Notification.show("User deleted");
            UI.getCurrent().navigate(UserListView.class);
        }
    }

    private String hashPassword(String rawPassword) {
        return rawPassword; // Replace with real hashing logic
    }
}
