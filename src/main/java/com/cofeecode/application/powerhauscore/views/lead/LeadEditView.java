package com.cofeecode.application.powerhauscore.views.lead;

import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.security.AuthenticatedUser;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.cofeecode.application.powerhauscore.views.project.ProjectEditView;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Edit Lead")
@Route(value = "leads/:projectID?/edit", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN", "HR"})
public class LeadEditView extends ProjectEditView {

    public LeadEditView(ProjectService projectService, AuthenticatedUser authenticatedUser) {
        super(projectService, authenticatedUser);
    }
}
