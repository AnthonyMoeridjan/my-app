package com.cofeecode.application.powerhauscore.views.quote;

import com.cofeecode.application.powerhauscore.customfield.Money;
import com.cofeecode.application.powerhauscore.customfield.PriceField;
import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.Quote;
import com.cofeecode.application.powerhauscore.data.QuoteStatus;
import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.services.QuoteService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.util.Optional;

@PageTitle("Edit Quote")
@Route(value = "quotes/:quoteID?/edit", layout = MainLayout.class)
@RolesAllowed({"USER","RVC", "HR", "ADMIN"})
public class QuoteEditView extends VerticalLayout implements BeforeEnterObserver {

    private final QuoteService quoteService;
    private final ProjectService projectService;
    private final BeanValidationBinder<Quote> binder = new BeanValidationBinder<>(Quote.class);

    private final TextField quoteNumber = new TextField("Quote Number");
    private final ComboBox<Project> project = new ComboBox<>("Project");
    private final TextField client = new TextField("Client");
    private final PriceField amount = new PriceField("Amount");
    private final DatePicker date = new DatePicker("Date");
    private final ComboBox<QuoteStatus> status = new ComboBox<>("Status", QuoteStatus.values());
    private final TextArea description = new TextArea("Description");

    private final Button saveButton = new Button("Save");
    private final Button cancelButton = new Button("Cancel", e -> UI.getCurrent().navigate(QuoteListView.class));

    private final DatePicker firstPaymentDate = new DatePicker("Date 1st Payment");
    private final PriceField firstPaymentAmount = new PriceField("Amount 1st Payment");

    private final DatePicker lastPaymentDate = new DatePicker("Date Last Payment");
    private final PriceField lastPaymentAmount = new PriceField("Amount Last Payment");

    private Quote quote;

    public QuoteEditView(QuoteService quoteService, ProjectService projectService) {
        this.quoteService = quoteService;
        this.projectService = projectService;

        addClassName("quote-edit-view");
        setSizeFull();

        add(new H2("Edit Quote"));
        add(createFormLayout());
        add(createPaymentSection());
        add(createButtonLayout());

        configureBinder();
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        project.setItems(projectService.findAll());
        project.setItemLabelGenerator(Project::getName);

        amount.setWidthFull();

        formLayout.add(quoteNumber, project);
        formLayout.add(client, status);
        formLayout.add(amount, date);
        formLayout.add(description);
//        formLayout.add(firstPaymentDate, lastPaymentDate);
//        formLayout.add(firstPaymentAmount, lastPaymentAmount);
        return formLayout;
    }

    private Component createPaymentSection() {
        FormLayout paymentForm = new FormLayout();
        paymentForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        paymentForm.add(firstPaymentDate, lastPaymentDate);
        paymentForm.add(firstPaymentAmount, lastPaymentAmount);

        Details paymentDetails = new Details("💳 Payment Info", paymentForm);
        paymentDetails.setOpened(true); // You can set to false if you want it collapsed by default
        return paymentDetails;
    }

    private HorizontalLayout createButtonLayout() {
        saveButton.getElement().setAttribute("theme", "primary");
        saveButton.addClickListener(e -> saveQuote());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        return buttons;
    }

    private void configureBinder() {
        binder.forField(amount)
                .withConverter(
                        money -> money != null ? new BigDecimal(money.getAmount()) : null,
                        bigDecimal -> bigDecimal != null ? new Money(bigDecimal.toString(), amount.getCurrency().getValue()) : null,
                        "Invalid amount")
                .bind(
                        Quote::getAmount,
                        (quote, bigDecimal) -> {
                            quote.setAmount(bigDecimal);
                            quote.setCurrency(amount.getCurrency().getValue());
                        });
        binder.forField(date).bind(Quote::getDate, Quote::setDate);

        binder.forField(firstPaymentAmount)
                .withConverter(
                        money -> money != null ? new BigDecimal(money.getAmount()) : null,
                        bigDecimal -> bigDecimal != null ? new Money(bigDecimal.toString(), firstPaymentAmount.getCurrency().getValue()) : null,
                        "Invalid amount")
                .bind(
                        Quote::getFirstPaymentAmount,
                        (quote, value) -> {
                            quote.setFirstPaymentAmount(value);
                            quote.setFirstPaymentCurrency(firstPaymentAmount.getCurrency().getValue());
                        });

        binder.forField(lastPaymentAmount)
                .withConverter(
                        money -> money != null ? new BigDecimal(money.getAmount()) : null,
                        bigDecimal -> bigDecimal != null ? new Money(bigDecimal.toString(), lastPaymentAmount.getCurrency().getValue()) : null,
                        "Invalid amount")
                .bind(
                        Quote::getLastPaymentAmount,
                        (quote, value) -> {
                            quote.setLastPaymentAmount(value);
                            quote.setLastPaymentCurrency(lastPaymentAmount.getCurrency().getValue());
                        });

        binder.forField(firstPaymentDate).bind(Quote::getFirstPaymentDate, Quote::setFirstPaymentDate);
        binder.forField(lastPaymentDate).bind(Quote::getLastPaymentDate, Quote::setLastPaymentDate);
        binder.bindInstanceFields(this);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> idParam = event.getRouteParameters().get("quoteID");

        if (idParam.isPresent()) {
            quote = quoteService.get(Long.parseLong(idParam.get())).orElse(null);
            if (quote == null) {
                Notification.show("Quote not found.", 3000, Notification.Position.BOTTOM_START);
                UI.getCurrent().navigate(QuoteListView.class);
                return;
            }
        } else {
            quote = new Quote();
            if (quote.getFirstPaymentCurrency() != null) {
                firstPaymentAmount.getCurrency().setValue(quote.getFirstPaymentCurrency());
            }
            if (quote.getLastPaymentCurrency() != null) {
                lastPaymentAmount.getCurrency().setValue(quote.getLastPaymentCurrency());
            }
        }

        // Set currency dropdown (if existing)
        if (quote.getCurrency() != null) {
            amount.getCurrency().setValue(quote.getCurrency());
        }

        binder.readBean(quote);
    }

    private void saveQuote() {
        try {
            binder.writeBean(quote);
            quoteService.save(quote);
            Notification.show("Quote saved successfully");
            UI.getCurrent().navigate(QuoteListView.class);
        } catch (Exception e) {
            Notification.show("Error saving quote: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}