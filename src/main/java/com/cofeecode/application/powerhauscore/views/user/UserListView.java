package com.cofeecode.application.powerhauscore.views.user;

import com.cofeecode.application.powerhauscore.data.Role;
import com.cofeecode.application.powerhauscore.data.User;
import com.cofeecode.application.powerhauscore.services.UserService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import java.util.Set;

@PageTitle("User Management")
@Route(value = "users", layout = MainLayout.class)
@RolesAllowed({"ADMIN"}) // Only allow admins
public class UserListView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public UserListView(UserService userService) {
        this.userService = userService;
        addClassName("user-management-view");
        setSizeFull();

        configureGrid();
        add(getToolbar(), grid);
        updateList();
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addColumn(User::getUsername).setHeader("Username").setAutoWidth(true);
        grid.addColumn(User::getName).setHeader("Full Name").setAutoWidth(true);
        grid.addColumn(user -> formatRoles(user.getRoles())).setHeader("Roles").setAutoWidth(true);
        grid.addColumn(user -> {
            if (user.getProfilePicture() != null) {
                Image img = new Image("data:image/jpeg;base64," +
                        java.util.Base64.getEncoder().encodeToString(user.getProfilePicture()), "Profile Picture");
                img.setWidth("40px");
                img.setHeight("40px");
                return img;
            } else {
                return new Image(); // Empty image placeholder
            }
        }).setHeader("Profile").setAutoWidth(true);

        grid.addComponentColumn(user -> {
            Button editButton = new Button("Edit", click ->
                    UI.getCurrent().navigate("users/edit/" + user.getId()) // Route with user ID
            );
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            return editButton;
        }).setHeader("Actions");
    }

    private HorizontalLayout getToolbar() {
        Button addUserButton = new Button("New User", click -> UI.getCurrent().navigate(UserEditView.class));
        addUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout toolbar = new HorizontalLayout(addUserButton);
        toolbar.setWidthFull();
        return toolbar;
    }

    private void updateList() {
        grid.setItems(query -> userService.list(PageRequest.of(query.getPage(), query.getPageSize(),
                VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
    }

    private String formatRoles(Set<Role> roles) {
        return roles != null ? String.join(", ", roles.stream().map(Enum::name).toList()) : "No Roles";
    }
}
