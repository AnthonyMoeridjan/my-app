package com.cofeecode.application.powerhauscore.views.login;

import com.cofeecode.application.powerhauscore.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        setAction("login");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("PowerHaus Core");
//        i18n.getHeader().setDescription("Login using user/user or admin/admin");
        i18n.setAdditionalInformation(null);

        addLoginListener(e ->{
            UI.getCurrent().navigate("transactions");
        });

        setI18n(i18n);



        // Create a Div to hold the logo and the title
        Div customHeader = new Div();
        customHeader.getStyle().set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center");

        // Add the logo image
        Image logo = new Image("images/PH_logo.png", "Company Logo");
//        logo.setHeight("100px");
//        logo.setWidth("300px");
        customHeader.add(logo);
        setTitle(logo);

        // Add the custom header to the LoginOverlay
        customHeader.getElement().setAttribute("slot", "header");
        getElement().appendChild(customHeader.getElement());


        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            setOpened(false);
            event.forwardTo("transactions");
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
