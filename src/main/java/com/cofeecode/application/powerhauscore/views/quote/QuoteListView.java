package com.cofeecode.application.powerhauscore.views.quote;

import com.cofeecode.application.powerhauscore.data.Quote;
import com.cofeecode.application.powerhauscore.services.QuoteService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Quotes")
@Route(value = "quotes", layout = MainLayout.class)
@RolesAllowed({"USER","RVC", "HR", "ADMIN"})
public class QuoteListView extends VerticalLayout {

    private final QuoteService quoteService;
    private final Grid<Quote> grid = new Grid<>(Quote.class, false);
    private final TextField quoteNumberFilter = new TextField("Quote #");
    private final TextField clientFilter = new TextField("Client");
    private final ComboBox<String> statusFilter = new ComboBox<>("Status");

    @Autowired
    public QuoteListView(QuoteService quoteService) {
        this.quoteService = quoteService;
        addClassName("quote-list-view");

        add(createFilterLayout());
        configureGrid();
        Button btn = new Button("New Quote", e -> UI.getCurrent().navigate(QuoteEditView.class));
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(btn, grid);
        updateList();
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addColumn(Quote::getQuoteNumber).setHeader("Quote Number").setAutoWidth(true).setSortable(true);
//        grid.addColumn(quote -> quote.getProject() != null ? quote.getProject().getName() : "-").setHeader("Project").setAutoWidth(true);
        grid.addColumn(Quote::getClient).setHeader("Client").setAutoWidth(true).setSortable(true);;
        grid.addColumn(Quote::getAmount).setHeader("Amount").setAutoWidth(true).setSortable(true);;
        grid.addColumn(Quote::getCurrency).setHeader("Amount").setAutoWidth(true).setSortable(true);;
        grid.addColumn(Quote::getDate).setHeader("Date").setAutoWidth(true).setSortable(true);;
        grid.addColumn(Quote::getStatus).setHeader("Status").setAutoWidth(true).setSortable(true);;

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate("quotes/" + event.getValue().getId() + "/edit");
            }
        });
    }
    private HorizontalLayout createFilterLayout() {
        quoteNumberFilter.setClearButtonVisible(true);
        clientFilter.setClearButtonVisible(true);

        statusFilter.setItems("PAID", "APPROVED", "REJECTED", "PENDING");
        statusFilter.setPlaceholder("All");
        statusFilter.setClearButtonVisible(true);

        quoteNumberFilter.addValueChangeListener(e -> updateList());
        clientFilter.addValueChangeListener(e -> updateList());
        statusFilter.addValueChangeListener(e -> updateList());

        HorizontalLayout filters = new HorizontalLayout(quoteNumberFilter, clientFilter, statusFilter);
        filters.setWidthFull();
        filters.setSpacing(true);
        return filters;
    }


    private void updateList() {
        String quoteNumber = quoteNumberFilter.getValue().trim().toLowerCase();
        String client = clientFilter.getValue().trim().toLowerCase();
        String status = statusFilter.getValue();

        grid.setItems(
                quoteService.listAll().stream()
                        .filter(quote ->
                                (quoteNumber.isEmpty() || quote.getQuoteNumber().toLowerCase().contains(quoteNumber)) &&
                                        (client.isEmpty() || quote.getClient().toLowerCase().contains(client)) &&
                                        (status == null || quote.getStatus().name().equals(status))
                        ).toList()
        );
    }
}
