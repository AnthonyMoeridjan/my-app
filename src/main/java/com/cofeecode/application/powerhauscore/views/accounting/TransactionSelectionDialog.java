package com.cofeecode.application.powerhauscore.views.accounting;

import com.cofeecode.application.powerhauscore.data.Transaction;
import com.cofeecode.application.powerhauscore.services.TransactionService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.cofeecode.application.powerhauscore.data.Currency;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TransactionSelectionDialog extends Dialog {

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final DatePicker dateFilter = new DatePicker();
    private final TextField amountFilter = new TextField();
    private final ComboBox<Currency> currencyFilter = new ComboBox<>();
    private final TransactionService transactionService;
    private final Consumer<Set<Transaction>> transactionConsumer;
    private final Button confirmButton = new Button("Confirm");

    public TransactionSelectionDialog(TransactionService transactionService, Consumer<Set<Transaction>> transactionConsumer) {
        this.transactionService = transactionService;
        this.transactionConsumer = transactionConsumer;

        configureGrid();
        configureFilters();

        confirmButton.addClickListener(e -> {
            transactionConsumer.accept(grid.getSelectedItems());
            close();
        });

        HorizontalLayout filterLayout = new HorizontalLayout(searchField, dateFilter, amountFilter, currencyFilter);
        filterLayout.setWidthFull();

        VerticalLayout layout = new VerticalLayout(filterLayout, grid, confirmButton);
        layout.setSizeFull();
        add(layout);
        setSizeFull();
    }

    private void configureGrid() {
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn("date").setHeader("Date").setSortable(true);
        grid.addColumn("description").setHeader("Description").setSortable(true);
        grid.addColumn("amount").setHeader("Amount").setSortable(true);
        grid.addColumn("currency").setHeader("Currency").setSortable(true);

        grid.setItems(transactionService.findAll());
    }

    private void configureFilters() {
        searchField.setPlaceholder("Search by description...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());

        dateFilter.setPlaceholder("Filter by date...");
        dateFilter.setClearButtonVisible(true);
        dateFilter.addValueChangeListener(e -> updateList());

        amountFilter.setPlaceholder("Filter by amount...");
        amountFilter.setClearButtonVisible(true);
        amountFilter.setValueChangeMode(ValueChangeMode.LAZY);
        amountFilter.addValueChangeListener(e -> updateList());

        currencyFilter.setPlaceholder("Filter by currency...");
        currencyFilter.setItems(Currency.values());
        currencyFilter.setItemLabelGenerator(Currency::name);
        currencyFilter.setClearButtonVisible(true);
        currencyFilter.addValueChangeListener(e -> updateList());
    }

    private void updateList() {
        grid.setItems(transactionService.findAll().stream()
                .filter(transaction -> {
                    boolean matchesDescription = searchField.getValue().isEmpty() || transaction.getDescription().toLowerCase().contains(searchField.getValue().toLowerCase());
                    boolean matchesDate = dateFilter.getValue() == null || (transaction.getDate() != null && transaction.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().equals(dateFilter.getValue()));
                    boolean matchesAmount = amountFilter.getValue().isEmpty() || (transaction.getAmount() != null && transaction.getAmount().toString().contains(amountFilter.getValue()));
                    boolean matchesCurrency = currencyFilter.getValue() == null || (transaction.getCurrency() != null && transaction.getCurrency().equalsIgnoreCase(currencyFilter.getValue().name()));
                    return matchesDescription && matchesDate && matchesAmount && matchesCurrency;
                })
                .collect(Collectors.toList()));
    }
}