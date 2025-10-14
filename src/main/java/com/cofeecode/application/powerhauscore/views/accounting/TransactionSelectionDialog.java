package com.cofeecode.application.powerhauscore.views.accounting;

import com.cofeecode.application.powerhauscore.data.Transaction;
import com.cofeecode.application.powerhauscore.services.TransactionService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TransactionSelectionDialog extends Dialog {

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final TransactionService transactionService;
    private final Consumer<Transaction> transactionConsumer;

    public TransactionSelectionDialog(TransactionService transactionService, Consumer<Transaction> transactionConsumer) {
        this.transactionService = transactionService;
        this.transactionConsumer = transactionConsumer;

        configureGrid();
        configureSearchField();

        VerticalLayout layout = new VerticalLayout(searchField, grid);
        layout.setSizeFull();
        add(layout);
        setSizeFull();
    }

    private void configureGrid() {
        grid.addColumn("date").setHeader("Date").setSortable(true);
        grid.addColumn("description").setHeader("Description").setSortable(true);
        grid.addColumn("amount").setHeader("Amount").setSortable(true);
        grid.addColumn("currency").setHeader("Currency").setSortable(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            transactionConsumer.accept(event.getValue());
            close();
        });

        grid.setItems(transactionService.findAll());
    }

    private void configureSearchField() {
        searchField.setPlaceholder("Search by description...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateList());
    }

    private void updateList() {
        grid.setItems(transactionService.findAll().stream()
                .filter(transaction -> transaction.getDescription().toLowerCase().contains(searchField.getValue().toLowerCase()))
                .collect(Collectors.toList()));
    }
}