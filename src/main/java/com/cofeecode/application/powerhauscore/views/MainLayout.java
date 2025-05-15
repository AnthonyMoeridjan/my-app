package com.cofeecode.application.powerhauscore.views;

import com.cofeecode.application.powerhauscore.data.User;
import com.cofeecode.application.powerhauscore.security.AuthenticatedUser;
import com.cofeecode.application.powerhauscore.security.InactivityLogoutHandler;
import com.cofeecode.application.powerhauscore.services.UserService;
import com.cofeecode.application.powerhauscore.views.Projects.ProjectListView;
import com.cofeecode.application.powerhauscore.views.checkoutform.CheckoutFormView;
import com.cofeecode.application.powerhauscore.views.quote.QuoteListView;
import com.cofeecode.application.powerhauscore.views.settings.SettingsView;
import com.cofeecode.application.powerhauscore.views.timesheet.TimesheetListView;
import com.cofeecode.application.powerhauscore.views.transaction.TransactionListView;
import com.cofeecode.application.powerhauscore.views.user.ChangePasswordDialog;
import com.cofeecode.application.powerhauscore.views.user.UserListView;
import com.cofeecode.application.powerhauscore.views.workers.WorkerMasterDetailView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private H1 viewTitle;

    private final UserService userService;
    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;
    private final InactivityLogoutHandler inactivityHandler = new InactivityLogoutHandler();

    public MainLayout(UserService userservice, AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.userService = userservice;
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("PowerHaus Core App");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

//        if (accessChecker.hasAccess(DashboardView.class)) {
//            nav.addItem(new SideNavItem("Dashboard", DashboardView.class, LineAwesomeIcon.CHART_BAR_SOLID.create()));
//
//        }
//        if (accessChecker.hasAccess(DataGridView.class)) {
//            nav.addItem(new SideNavItem("Data Grid", DataGridView.class, LineAwesomeIcon.TH_SOLID.create()));
//
//        }
        if (accessChecker.hasAccess(WorkerMasterDetailView.class)) {
            nav.addItem(
                    new SideNavItem("Workers", WorkerMasterDetailView.class, LineAwesomeIcon.USERS_SOLID.create()));

        }
        if (accessChecker.hasAccess(TimesheetListView.class)) {
            nav.addItem(
                    new SideNavItem("Timesheet", TimesheetListView.class, LineAwesomeIcon.STOPWATCH_SOLID.create()));

        }
        if (accessChecker.hasAccess(CheckoutFormView.class)) {
            nav.addItem(new SideNavItem("Checkout Form", CheckoutFormView.class, LineAwesomeIcon.CREDIT_CARD.create()));

        }
        if (accessChecker.hasAccess(ProjectListView.class)) {
            nav.addItem(
                    new SideNavItem("Project-List", ProjectListView.class, LineAwesomeIcon.CALENDAR.create()));
        }
        if (accessChecker.hasAccess(UserListView.class)) {
            nav.addItem(
                    new SideNavItem("Users", UserListView.class, LineAwesomeIcon.USER_COG_SOLID.create()));
        }
        if (accessChecker.hasAccess(TransactionListView.class)) {
            nav.addItem(
                    new SideNavItem("Transactions", TransactionListView.class, LineAwesomeIcon.MONEY_BILL_SOLID.create()));
        }
        if (accessChecker.hasAccess(QuoteListView.class)) {
            nav.addItem(
                    new SideNavItem("Offerte Lijst", QuoteListView.class, LineAwesomeIcon.FILE_INVOICE_DOLLAR_SOLID.create()));
        }
        if (accessChecker.hasAccess(SettingsView.class)) {
            nav.addItem(
                    new SideNavItem("Settings", SettingsView.class, LineAwesomeIcon.COG_SOLID.create()));
        }

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getName());
            StreamResource resource = new StreamResource("profile-pic",
                    () -> new ByteArrayInputStream(user.getProfilePicture()));
            avatar.setImageResource(resource);
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Change Password", e -> {
                ChangePasswordDialog dialog = new ChangePasswordDialog(authenticatedUser, userService);
                dialog.open();
            });
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        inactivityHandler.afterNavigation(event);
    }

}
