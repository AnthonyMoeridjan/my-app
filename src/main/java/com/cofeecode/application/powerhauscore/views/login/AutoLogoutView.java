package com.cofeecode.application.powerhauscore.views.login;

import com.cofeecode.application.powerhauscore.security.AuthenticatedUser;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("auto-logout")
@AnonymousAllowed
public class AutoLogoutView extends VerticalLayout implements AfterNavigationObserver {

    private final AuthenticatedUser authenticatedUser;

    public AutoLogoutView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        authenticatedUser.logout();
    }
}
