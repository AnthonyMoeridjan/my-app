package com.cofeecode.application.powerhauscore.views.transaction;

import com.cofeecode.application.powerhauscore.customfield.PriceField;
import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.Transaction;
import com.cofeecode.application.powerhauscore.data.TransactionType;
import com.cofeecode.application.powerhauscore.services.TransactionService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.math.BigDecimal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.router.QueryParameters;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@PageTitle("Transactions")
@Route(value = "transactions", layout = MainLayout.class)
@RolesAllowed({"USER","RVC", "HR", "ADMIN"})
@Uses(Icon.class)
public class TransactionListView extends VerticalLayout implements BeforeEnterObserver{

    private final String TRANSACTION_EDIT_ROUTE_TEMPLATE = "transactions/%s/edit";

    private final TransactionService transactionService;
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);

    private Dialog filterDialog;
    private DatePicker startDateFilter;
    private DatePicker endDateFilter;
    private ComboBox<TransactionType> typeFilter;
    private ComboBox<Project> projectFilter;
    private ComboBox<String> dagboekFilter;
    private TextField extraFilter;
    private TextField categoryFilter;
    private TextField descriptionFilter;

    private final Span srdTotal = new Span();
    private final Span usdTotal = new Span();
    private final Span eurTotal = new Span();

    private Specification<Transaction> currentSpec;

    private final HorizontalLayout filterFeedback = new HorizontalLayout();
    private final Span filterStatus = new Span("Bedragen zijn gefilterd");

    public TransactionListView(TransactionService transactionService) {
        this.transactionService = transactionService;
        this.currentSpec = (root, query, cb) -> cb.conjunction(); // Initialize to select all

        startDateFilter = new DatePicker("From");
        endDateFilter = new DatePicker("To");
        typeFilter = new ComboBox<>("Type");
        typeFilter.setItems(TransactionType.values());
        typeFilter.setItemLabelGenerator(type -> type == TransactionType.DEBIT ? "Uitgaven" : "Inkomsten");

        projectFilter = new ComboBox<>("Project");
        projectFilter.setItems(transactionService.findAllProjects());
        projectFilter.setItemLabelGenerator(Project::getName);

        dagboekFilter = new ComboBox<>("Dagboek");
        dagboekFilter.setItems("Kas", "Bank");

        extraFilter = new TextField("Extra Info");
        categoryFilter = new TextField("Category");
        descriptionFilter = new TextField("Description");

        setSizeFull();
        addClassName("transaction-list-view");
        getStyle().set("padding", "var(--lumo-space-l)");

        add(buildHeader());
        add(buildFilterFeedback());
//        add(buildFilterPresets());
        add(buildTopBar());
        add(buildGridCard());

        updateTotals(transactionService.findAll());
    }

    private Component buildFilterPresets() {
        HorizontalLayout presetBar = new HorizontalLayout();
        presetBar.setSpacing(false);
        presetBar.getStyle().set("gap", "0.5rem");
        presetBar.setAlignItems(FlexComponent.Alignment.CENTER);
        presetBar.getStyle().set("margin-bottom", "0.5rem");

        Button thisMonthBtn = new Button("This Month", e -> {
            LocalDate now = LocalDate.now();
            startDateFilter.setValue(now.withDayOfMonth(1));
            endDateFilter.setValue(now);
            updateFilteredGrid();
        });

        Button last30DaysBtn = new Button("Last 30 Days", e -> {
            LocalDate now = LocalDate.now();
            startDateFilter.setValue(now.minusDays(30));
            endDateFilter.setValue(now);
            updateFilteredGrid();
        });

        Button thisYearBtn = new Button("This Year", e -> {
            LocalDate now = LocalDate.now();
            startDateFilter.setValue(now.withDayOfYear(1));
            endDateFilter.setValue(now);
            updateFilteredGrid();
        });

        Button presetToday = new Button("Today", e -> {
            LocalDate today = LocalDate.now();
            startDateFilter.setValue(today);
            endDateFilter.setValue(today);
            updateFilteredGrid();
        });

        Button clearBtn = new Button("Clear", e -> {
            startDateFilter.clear();
            endDateFilter.clear();
            updateFilteredGrid();
        });

        presetToday.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        thisMonthBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        last30DaysBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        thisYearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        presetBar.add(presetToday, thisMonthBtn, thisYearBtn, clearBtn);
        return presetBar;
    }

    private Component buildHeader() {
        HorizontalLayout layout = new HorizontalLayout(
                createTotalCard("SRD", srdTotal),
                createTotalCard("USD", usdTotal),
                createTotalCard("EUR", eurTotal)
        );
        layout.setWidthFull();
        layout.setPadding(false);
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.getStyle().set("gap", "0.5rem");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layout.addClassName(LumoUtility.Margin.Bottom.SMALL);
        return layout;
    }

    private Component buildTopBar() {
        Button newTransaction = new Button("New Transaction", e ->
                UI.getCurrent().navigate("transactions/edit"));
        newTransaction.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Icon filterIcon = VaadinIcon.FILTER.create();
        Button filterButton = new Button("Filter", filterIcon);
        filterButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        filterButton.addClickListener(e -> openFilterDialog());

        Button thisMonthBtn = new Button("This Month", e -> {
            LocalDate now = LocalDate.now();
            startDateFilter.setValue(now.withDayOfMonth(1));
            endDateFilter.setValue(now);
            updateFilteredGrid();
        });

        Button last30DaysBtn = new Button("Last 30 Days", e -> {
            LocalDate now = LocalDate.now();
            startDateFilter.setValue(now.minusDays(30));
            endDateFilter.setValue(now);
            updateFilteredGrid();
        });

        Button thisYearBtn = new Button("This Year", e -> {
            LocalDate now = LocalDate.now();
            startDateFilter.setValue(now.withDayOfYear(1));
            endDateFilter.setValue(now);
            updateFilteredGrid();
        });

        Button presetToday = new Button("Today", e -> {
            LocalDate today = LocalDate.now();
            startDateFilter.setValue(today);
            endDateFilter.setValue(today);
            updateFilteredGrid();
        });

        Button clearBtn = new Button("Clear", e -> {
            startDateFilter.clear();
            endDateFilter.clear();
            updateFilteredGrid();
        });

        Button presetKas = new Button("Kas", e -> {
            dagboekFilter.setValue("Kas");
            updateFilteredGrid();
        });

        Button presetBank = new Button("Bank", e -> {
            dagboekFilter.setValue("Bank");
            updateFilteredGrid();
        });

        presetToday.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        thisMonthBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        last30DaysBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        thisYearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        presetKas.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        presetBank.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        Button exportButton = new Button("Export to XLSX", VaadinIcon.DOWNLOAD_ALT.create());
        exportButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        exportButton.addClickListener(e -> {
            ByteArrayOutputStream baos = generateTransactionsXlsx();
            if (baos != null && baos.size() > 0) {
                String fileName = "transactions_" + System.currentTimeMillis() + ".xlsx";
                StreamResource streamResource = new StreamResource(fileName, () -> new ByteArrayInputStream(baos.toByteArray()));
                streamResource.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                streamResource.setCacheTime(0);

                Anchor downloadLink = new Anchor(streamResource, "");
                downloadLink.getElement().setAttribute("download", true);
                downloadLink.getStyle().set("display", "none");
                add(downloadLink); // Add to the UI to be clickable
                UI.getCurrent().getPage().executeJs("$0.click()", downloadLink.getElement());

                // Use a then() block to remove the link after the JS execution is likely done.
                // This is more robust than immediate removal.
                UI.getCurrent().getPage().executeJs("1+1").then(r -> remove(downloadLink));


                Notification.show("Export started: " + fileName, 3000, Notification.Position.BOTTOM_START);
            } else {
                Notification.show("Failed to generate XLSX or no data to export.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // exportButton is already defined above with its click listener

        HorizontalLayout leftLayout = new HorizontalLayout(newTransaction, presetToday, thisMonthBtn, thisYearBtn, presetKas, presetBank);
        leftLayout.setSpacing(true);
        leftLayout.setPadding(false);

        // Create a new layout for the right side buttons to group them
        HorizontalLayout rightButtonLayout = new HorizontalLayout(exportButton, filterButton);
        rightButtonLayout.setSpacing(true); // Add spacing between export and filter buttons
        rightButtonLayout.setPadding(false);


        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(JustifyContentMode.BETWEEN); // This will push leftLayout to left and rightButtonLayout to right
        layout.setWidthFull();
        // layout.setSpacing(true); // Spacing is handled by inner layouts or can be adjusted here if needed for overall layout
        layout.setPadding(false);
        layout.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        // Add components in order
        layout.add(leftLayout, rightButtonLayout); // Add the new group to the main layout

        return layout;
    }

    private Component buildFilterFeedback() {
        filterStatus.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.9em")
                .set("font-style", "italic");

        Button clearFilters = new Button("Clear Filters", VaadinIcon.CLOSE_CIRCLE.create(), e -> {
            startDateFilter.clear();
            endDateFilter.clear();
            typeFilter.clear();
            projectFilter.clear();
            dagboekFilter.clear();
            extraFilter.clear();
            categoryFilter.clear();
            descriptionFilter.clear();
            updateFilteredGrid();
        });
        clearFilters.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        filterFeedback.setVisible(false);
        filterFeedback.add(filterStatus, clearFilters);
        filterFeedback.setAlignItems(FlexComponent.Alignment.CENTER);
        return filterFeedback;
    }

    private Component buildGridCard() {
        configureGrid();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();

        Div card = new Div(grid);
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,0.05)")
                .set("padding", "1rem")
                .set("flex-grow", "1");
        card.setWidthFull();

        return card;
    }

    private Component createTotalCard(String label, Span totalSpan) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background-color", "#f9f9f9")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,0.05)")
                .set("min-width", "160px");

        Span header = new Span(label);
        header.getStyle().set("font-weight", "bold").set("font-size", "0.9em");
        totalSpan.getStyle().set("font-weight", "bold").set("font-size", "1.1em");

        card.add(header, totalSpan);
        return card;
    }

    private void configureGrid() {
        grid.addColumn("dagboek").setHeader("Dagboek").setAutoWidth(true);
        Grid.Column<Transaction> dateColumn = grid.addColumn("date")
                .setHeader("Date")
                .setAutoWidth(true)
                .setSortable(true);
//        grid.addColumn("date").setHeader("Date").setAutoWidth(true);
        grid.addColumn(t -> t.getTransactionType() == TransactionType.DEBIT ? "Uitgaven" : "Inkomsten")
                .setHeader("Type")
                .setAutoWidth(true)
                .setSortable(true)
                .setSortProperty("transactionType");
        grid.addColumn("currency").setHeader("Currency").setAutoWidth(true);
        grid.addColumn("amount").setHeader("Amount").setAutoWidth(true);
        grid.addColumn("description").setHeader("Description").setAutoWidth(true);
        grid.addColumn("category").setHeader("Category").setAutoWidth(true);
        grid.addColumn(t -> t.getProject() != null ? t.getProject().getName() : "-")
                .setHeader("Project").setAutoWidth(true);
        grid.addColumn("btw").setHeader("BTW").setAutoWidth(true);
        grid.addColumn("extra").setHeader("Extra Info").setAutoWidth(true);

        grid.setItems(query -> transactionService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query))
        ).stream());

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                String route = String.format(TRANSACTION_EDIT_ROUTE_TEMPLATE, event.getValue().getId());
                List<String> queryParams = new ArrayList<>();

                try {
                    // Text filters
                    if (extraFilter.getValue() != null && !extraFilter.getValue().isEmpty()) {
                        queryParams.add("extraFilter=" + URLEncoder.encode(extraFilter.getValue(), StandardCharsets.UTF_8.name()));
                    }
                    if (categoryFilter.getValue() != null && !categoryFilter.getValue().isEmpty()) {
                        queryParams.add("categoryFilter=" + URLEncoder.encode(categoryFilter.getValue(), StandardCharsets.UTF_8.name()));
                    }
                    if (descriptionFilter.getValue() != null && !descriptionFilter.getValue().isEmpty()) {
                        queryParams.add("descriptionFilter=" + URLEncoder.encode(descriptionFilter.getValue(), StandardCharsets.UTF_8.name()));
                    }

                    // Date filters
                    if (startDateFilter.getValue() != null) {
                        queryParams.add("startDate=" + URLEncoder.encode(startDateFilter.getValue().toString(), StandardCharsets.UTF_8.name()));
                    }
                    if (endDateFilter.getValue() != null) {
                        queryParams.add("endDate=" + URLEncoder.encode(endDateFilter.getValue().toString(), StandardCharsets.UTF_8.name()));
                    }

                    // ComboBox filters
                    if (typeFilter.getValue() != null) {
                        queryParams.add("type=" + URLEncoder.encode(typeFilter.getValue().name(), StandardCharsets.UTF_8.name()));
                    }
                    if (projectFilter.getValue() != null && projectFilter.getValue().getId() != null) {
                        queryParams.add("projectId=" + URLEncoder.encode(String.valueOf(projectFilter.getValue().getId()), StandardCharsets.UTF_8.name()));
                    }
                    if (dagboekFilter.getValue() != null && !dagboekFilter.getValue().isEmpty()) {
                        queryParams.add("dagboek=" + URLEncoder.encode(dagboekFilter.getValue(), StandardCharsets.UTF_8.name()));
                    }

                } catch (Exception e) {
                    // Broad exception catch for URLEncoder.encode, consider more specific handling or logging
                    System.err.println("Error encoding query parameters: " + e.getMessage());
                }

                if (!queryParams.isEmpty()) {
                    route += "?" + String.join("&", queryParams);
                }
                UI.getCurrent().navigate(route);
            }
        });

        grid.sort(List.of(new GridSortOrder<>(dateColumn, SortDirection.DESCENDING)));
    }

    private void openFilterDialog() {
        if (filterDialog == null) {
            filterDialog = new Dialog();
            filterDialog.setHeaderTitle("Filter Transactions");

            FormLayout formLayout = new FormLayout();
            formLayout.add(startDateFilter, endDateFilter, typeFilter, projectFilter, dagboekFilter, extraFilter, categoryFilter, descriptionFilter);
            formLayout.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("600px", 2),
                    new FormLayout.ResponsiveStep("900px", 3)
            );
            formLayout.getStyle()
                    .set("font-size", "0.85em")
                    .set("gap", "0.5rem");

            Button apply = new Button("Apply", e -> {
                updateFilteredGrid();
                filterDialog.close();
            });
            apply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            Button close = new Button("Cancel", e -> filterDialog.close());

            HorizontalLayout actions = new HorizontalLayout(apply, close);
            actions.setSpacing(true);
            actions.getStyle().set("margin-top", "0.5rem");

            VerticalLayout layout = new VerticalLayout(formLayout, actions);
            layout.setPadding(false);
            layout.setSpacing(false);

            filterDialog.add(layout);
        }
        filterDialog.open();
    }

    private void updateFilteredGrid() {
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDateFilter.getValue() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), Date.valueOf(startDateFilter.getValue())));
            }
            if (endDateFilter.getValue() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), Date.valueOf(endDateFilter.getValue())));
            }
            if (typeFilter.getValue() != null) {
                predicates.add(cb.equal(root.get("transactionType"), typeFilter.getValue()));
            }
            if (projectFilter.getValue() != null) {
                predicates.add(cb.equal(root.get("project"), projectFilter.getValue()));
            }
            if (dagboekFilter.getValue() != null && !dagboekFilter.getValue().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("dagboek")), dagboekFilter.getValue().toLowerCase()));
            }
            if (extraFilter.getValue() != null && !extraFilter.getValue().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("extra")), "%" + extraFilter.getValue().trim().toLowerCase() + "%"));
            }
            if (categoryFilter.getValue() != null && !categoryFilter.getValue().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("category")), "%" + categoryFilter.getValue().trim().toLowerCase() + "%"));
            }
            if (descriptionFilter.getValue() != null && !descriptionFilter.getValue().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + descriptionFilter.getValue().trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        this.currentSpec = spec; // Update the current specification

        boolean hasFilters = startDateFilter.getValue() != null
                || endDateFilter.getValue() != null
                || typeFilter.getValue() != null
                || projectFilter.getValue() != null
                || (dagboekFilter.getValue() != null && !dagboekFilter.getValue().trim().isEmpty())
                || (extraFilter.getValue() != null && !extraFilter.getValue().trim().isEmpty())
                || (categoryFilter.getValue() != null && !categoryFilter.getValue().trim().isEmpty())
                || (descriptionFilter.getValue() != null && !descriptionFilter.getValue().trim().isEmpty());

        filterFeedback.setVisible(hasFilters);

        List<Transaction> filtered = transactionService.findAll(spec);
        updateTotals(filtered);

        grid.setItems(query -> transactionService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                spec
        ).stream());
    }

    private void updateTotals(List<Transaction> transactions) {
        BigDecimal srd = BigDecimal.ZERO;
        BigDecimal usd = BigDecimal.ZERO;
        BigDecimal eur = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            BigDecimal amt = t.getTransactionType() == TransactionType.DEBIT ? t.getAmount().negate() : t.getAmount();
            switch (t.getCurrency().toUpperCase()) {
                case "SRD" -> srd = srd.add(amt);
                case "USD" -> usd = usd.add(amt);
                case "EUR", "EURO" -> eur = eur.add(amt);
            }
        }

        srdTotal.setText(srd.toString());
        usdTotal.setText(usd.toString());
        eurTotal.setText(eur.toString());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();

        boolean localFiltersAppliedFromUrl = false;

        // Text filters
        String extraFilterParam = parametersMap.getOrDefault("extraFilter", List.of()).stream().findFirst().orElse(null);
        if (extraFilterParam != null) {
            extraFilter.setValue(extraFilterParam);
            localFiltersAppliedFromUrl = true;
        }

        String categoryFilterParam = parametersMap.getOrDefault("categoryFilter", List.of()).stream().findFirst().orElse(null);
        if (categoryFilterParam != null) {
            categoryFilter.setValue(categoryFilterParam);
            localFiltersAppliedFromUrl = true;
        }

        String descriptionFilterParam = parametersMap.getOrDefault("descriptionFilter", List.of()).stream().findFirst().orElse(null);
        if (descriptionFilterParam != null) {
            descriptionFilter.setValue(descriptionFilterParam);
            localFiltersAppliedFromUrl = true;
        }

        // Date filters
        String startDateParam = parametersMap.getOrDefault("startDate", List.of()).stream().findFirst().orElse(null);
        if (startDateParam != null) {
            try {
                startDateFilter.setValue(LocalDate.parse(startDateParam));
                localFiltersAppliedFromUrl = true;
            } catch (DateTimeParseException e) {
                System.err.println("Error parsing startDate: " + startDateParam + " - " + e.getMessage());
            }
        }

        String endDateParam = parametersMap.getOrDefault("endDate", List.of()).stream().findFirst().orElse(null);
        if (endDateParam != null) {
            try {
                endDateFilter.setValue(LocalDate.parse(endDateParam));
                localFiltersAppliedFromUrl = true;
            } catch (DateTimeParseException e) {
                System.err.println("Error parsing endDate: " + endDateParam + " - " + e.getMessage());
            }
        }

        // ComboBox filters
        String typeParam = parametersMap.getOrDefault("type", List.of()).stream().findFirst().orElse(null);
        if (typeParam != null) {
            try {
                typeFilter.setValue(TransactionType.valueOf(typeParam));
                localFiltersAppliedFromUrl = true;
            } catch (IllegalArgumentException e) {
                System.err.println("Error parsing type: " + typeParam + " - " + e.getMessage());
            }
        }

        String projectIdParam = parametersMap.getOrDefault("projectId", List.of()).stream().findFirst().orElse(null);
        if (projectIdParam != null) {
            try {
                Long pId = Long.parseLong(projectIdParam);
                Optional<Project> projectToSelect = projectFilter.getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>())
                        .filter(p -> p.getId().equals(pId))
                        .findFirst();
                if (projectToSelect.isPresent()) {
                    projectFilter.setValue(projectToSelect.get());
                    localFiltersAppliedFromUrl = true;
                } else {
                    System.err.println("Project with ID: " + projectIdParam + " not found in projectFilter items.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing projectId: " + projectIdParam + " - " + e.getMessage());
            }
        }

        String dagboekParam = parametersMap.getOrDefault("dagboek", List.of()).stream().findFirst().orElse(null);
        if (dagboekParam != null) {
            dagboekFilter.setValue(dagboekParam);
            localFiltersAppliedFromUrl = true;
        }

        if (localFiltersAppliedFromUrl) {
            updateFilteredGrid();
        }
    }

    private ByteArrayOutputStream generateTransactionsXlsx() {
        List<Transaction> transactionsToExport = transactionService.findAll(this.currentSpec);

        if (transactionsToExport.isEmpty()) {
            return null; // Or an empty stream
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");

        // Header Font and Style
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);

        // Date Cell Style
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

        // Header Row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Dagboek", "Date", "Type", "Currency", "Amount", "Description", "Category", "Project", "BTW", "Extra Info"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data Rows
        int rowIndex = 1;
        for (Transaction tx : transactionsToExport) {
            Row dataRow = sheet.createRow(rowIndex++);

            dataRow.createCell(0).setCellValue(tx.getDagboek() != null ? tx.getDagboek() : "");

            Cell dateCell = dataRow.createCell(1);
            if (tx.getDate() != null) {
                dateCell.setCellValue(tx.getDate());
                dateCell.setCellStyle(dateCellStyle);
            } else {
                dateCell.setCellValue("");
            }

            dataRow.createCell(2).setCellValue(tx.getTransactionType() != null ? (tx.getTransactionType() == TransactionType.DEBIT ? "Uitgaven" : "Inkomsten") : "");
            dataRow.createCell(3).setCellValue(tx.getCurrency() != null ? tx.getCurrency() : "");

            Cell amountCell = dataRow.createCell(4);
            if (tx.getAmount() != null) {
                amountCell.setCellValue(tx.getAmount().doubleValue());
            } else {
                amountCell.setCellValue(0.0);
            }

            dataRow.createCell(5).setCellValue(tx.getDescription() != null ? tx.getDescription() : "");
            dataRow.createCell(6).setCellValue(tx.getCategory() != null ? tx.getCategory() : "");
            dataRow.createCell(7).setCellValue(tx.getProject() != null && tx.getProject().getName() != null ? tx.getProject().getName() : "");

            Cell btwCell = dataRow.createCell(8);
            if (tx.getBtw() != null) {
                btwCell.setCellValue(tx.getBtw().doubleValue());
            } else {
                btwCell.setCellValue(0.0);
            }
            dataRow.createCell(9).setCellValue(tx.getExtra() != null ? tx.getExtra() : "");
        }

        // Autosize columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
        } catch (IOException e) {
            e.printStackTrace(); // Proper logging would be better
            // Consider notifying the user or returning null
            return null;
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace(); // Proper logging
            }
        }
        return baos;
    }
}