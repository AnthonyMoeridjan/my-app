package com.cofeecode.application.powerhauscore.views.transaction;

import com.cofeecode.application.powerhauscore.customfield.Money;
import com.cofeecode.application.powerhauscore.customfield.PriceField;
import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.Transaction;
import com.cofeecode.application.powerhauscore.data.TransactionType;
import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.services.SettingsService;
import com.cofeecode.application.powerhauscore.services.TransactionService;
import com.cofeecode.application.powerhauscore.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//@PageTitle("Edit Transaction")
@Route(value = "transactions/:transactionID?/edit", layout = MainLayout.class)
@RolesAllowed({"USER"/*,"RVC"*/, "HR", "ADMIN"})
public class TransactionEditView extends Div implements BeforeEnterObserver {

    private final String TRANSACTION_ID = "transactionID";
    private static final String UPLOAD_DIRECTORY = "D:/Java Projects/my-app/uploads"; // Directory where uploaded files will be stored

    // Fields to store filter parameters
    private String extraFilterValue;
    private String categoryFilterValue;
    private String descriptionFilterValue;
    private String startDateValue;
    private String endDateValue;
    private String typeValue;
    private String projectIdValue;
    private String dagboekValue;
    private final TransactionService transactionService;
    private final ProjectService projectService;
    private final SettingsService settingsService;
    private Transaction transaction;
    private BeanValidationBinder<Transaction> binder;

    // Form fields
    private DatePicker date = new DatePicker("Date");
    private PriceField amount = new PriceField("Amount");
    private TextField description = new TextField("Description");
    private PriceField btw = new PriceField("BTW");
    private ComboBox<Dagboek> dagboek = new ComboBox("Dagboek");
    private ComboBox<CategorieItems> category = new ComboBox("Category");
    private ComboBox<Lener> lener = new ComboBox("Persoon");
    private ComboBox<Project> project = new ComboBox<>("Project");
    private RadioButtonGroup<TransactionType> transactionTypeRadio = new RadioButtonGroup<>();

    private final Button save = new Button("Save", event -> save());
    private final Button cancel = new Button("Cancel", event -> cancel());
    private final Button delete = new Button("Delete", event -> deleteTransaction());


    private Upload upload;
    private FileBuffer fileBuffer;
    private Image imagePreview;
    private IFrame  pdfViewer;
    private String uploadedFilePath;

    private OrderedList imageContainer;

    private final Button deleteFileButton = new Button("Delete File", event -> deleteFile());

    public TransactionEditView(TransactionService transactionService, ProjectService projectService, SettingsService settingsService) {
        this.transactionService = transactionService;
        this.projectService = projectService;
        this.settingsService = settingsService;
        addClassName("edit-view");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Read filter parameters
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();

        extraFilterValue = parametersMap.getOrDefault("extraFilter", List.of()).stream().findFirst().orElse(null);
        categoryFilterValue = parametersMap.getOrDefault("categoryFilter", List.of()).stream().findFirst().orElse(null);
        descriptionFilterValue = parametersMap.getOrDefault("descriptionFilter", List.of()).stream().findFirst().orElse(null);
        startDateValue = parametersMap.getOrDefault("startDate", List.of()).stream().findFirst().orElse(null);
        endDateValue = parametersMap.getOrDefault("endDate", List.of()).stream().findFirst().orElse(null);
        typeValue = parametersMap.getOrDefault("type", List.of()).stream().findFirst().orElse(null);
        projectIdValue = parametersMap.getOrDefault("projectId", List.of()).stream().findFirst().orElse(null);
        dagboekValue = parametersMap.getOrDefault("dagboek", List.of()).stream().findFirst().orElse(null);

        Optional<Long> transactionId = event.getRouteParameters().get(TRANSACTION_ID).map(Long::parseLong);
        if (transactionId.isPresent()) {
            Optional<Transaction> transactionFromBackend = transactionService.get(transactionId.get());
            if (transactionFromBackend.isPresent()) {
                transaction = transactionFromBackend.get();
            } else {
                Notification.show("Transaction not found", 3000, Notification.Position.BOTTOM_START);
                // Navigate back to list view, potentially with filters if they were passed
                cancel();
                return;
            }
        } else {
            transaction = new Transaction();
        }
        createForm();
        updatePageTitle();
        populateForm();
    }


    private void createForm() {

        HorizontalLayout hl1 = new HorizontalLayout();
        hl1.setWidthFull();
//        hl1.setHeight("40px");
        hl1.setAlignItems(FlexComponent.Alignment.START);
        hl1.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        hl1.setPadding(true);
        hl1.addClassName(LumoUtility.Gap.MEDIUM);
        hl1.addClassNames(LumoUtility.Padding.Bottom.NONE);


        transactionTypeRadio.setLabel("Type");
        transactionTypeRadio.setItems(TransactionType.CREDIT, TransactionType.DEBIT);
        transactionTypeRadio.setItemLabelGenerator(type -> type == TransactionType.DEBIT ? "Uitgaven" : "Inkomsten");
        transactionTypeRadio.setValue(TransactionType.CREDIT);

        Paragraph textSmall = new Paragraph("SRD 0.00 $ 0.00 Euro 0.00");
        textSmall.setWidth("20em");
        textSmall.getStyle().set("font-size", "var(--lumo-font-size-xs)");

        hl1.add(transactionTypeRadio);
        add(hl1);

        HorizontalLayout hl2 = new HorizontalLayout();
        hl2.setWidthFull();
        hl2.getStyle().set("flex-grow", "1");
        hl2.addClassName(LumoUtility.Gap.MEDIUM);

        VerticalLayout vl21 = new VerticalLayout();
        VerticalLayout vl211 = new VerticalLayout();
        HorizontalLayout hl2111 = new HorizontalLayout();
        hl2111.setWidthFull();
        hl2111.setHeight("90px");
        hl2111.setMaxWidth("38em");
        hl2111.setAlignItems(FlexComponent.Alignment.START);
        hl2111.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        vl211.setPadding(false);
        vl211.setMargin(false);

        date.getStyle().set("flex-grow", "1");
        date.setValue(LocalDate.now());

        dagboek.getStyle().set("flex-grow", "1");
        setDagboekData(dagboek);

        category.getStyle().set("flex-grow", "1");
        setCategoryData(category);

        hl2111.add(date, dagboek, category);

        vl211.add(hl2111);

        HorizontalLayout hl2112 = new HorizontalLayout();
        hl2112.setWidthFull();
        hl2112.setHeight("90px");
        hl2112.setMaxWidth("38em");
        hl2112.setAlignItems(FlexComponent.Alignment.START);
        hl2112.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        lener.getStyle().set("flex-grow", "1");
        lener.getStyle().set("margin-left", "var(--lumo-space-m)");

        setLenerData(lener);
        hl2112.add(lener);

        category.addValueChangeListener(e -> {
            if(e.getValue().name().equalsIgnoreCase("Lening")){
                vl211.add(hl2112);
            } else {
                vl211.remove(hl2112);
                vl211.add(hl2111);
            }
        });


        HorizontalLayout hl212 = new HorizontalLayout();
        hl212.setWidthFull();
        hl212.setHeight("90px");
        hl212.addClassName(LumoUtility.Gap.MEDIUM);

        amount.setWidth("min-content");
        btw.setWidth("min-content");

        description.setWidth("100%");
        description.setMaxWidth("38em");

        hl212.add(amount,btw);
        hl212.setAlignSelf(FlexComponent.Alignment.START, description);

        vl21.setWidth("min-content");
        vl21.setHeightFull();
//        vl21.getStyle().set("flex-grow", "1");
        vl21.add(vl211,hl212,description);

        VerticalLayout vl22 = new VerticalLayout();
        vl22.setWidth("min-content");
        // Initialize file buffer to handle uploaded files
        fileBuffer = new FileBuffer();
        upload = new Upload(fileBuffer);
        upload.setAcceptedFileTypes("image/jpeg", "application/pdf");

// Image preview for JPG files
        imagePreview = new Image();
        imagePreview.setVisible(false);
        imagePreview.setMaxWidth("200px");

// Image preview
        imagePreview = new Image();
        imagePreview.setVisible(false);
        imagePreview.setMaxWidth("400px");

// PDF viewer (embedded in page)
        pdfViewer = new IFrame();
        pdfViewer.setVisible(false);
        pdfViewer.setSizeFull();  // Makes it responsive

// Handle file upload success
        upload.addSucceededListener(event -> {
            try {
                // Get the original uploaded filename
                String originalFileName = event.getFileName();

                // Get the upload directory from settings
                String userDefinedPath = settingsService.getUploadDirectory();
                File uploadDir = new File(userDefinedPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs(); // create the folder if it doesn't exist
                }

                // Save the file temporarily under the original name
                File tempFile = new File(uploadDir, originalFileName);
                try (InputStream inputStream = fileBuffer.getInputStream();
                     OutputStream outputStream = new FileOutputStream(tempFile)) {
                    inputStream.transferTo(outputStream);
                }

                // Store the temporary file path for renaming later
                uploadedFilePath = tempFile.getAbsolutePath();

                // Build preview URL (assumes /uploads maps to your upload directory)
                String fileUrl = "/uploads/" + tempFile.getName();

                // Show preview: image or PDF
                if (originalFileName.toLowerCase().endsWith(".jpg")
                        || originalFileName.toLowerCase().endsWith(".jpeg")
                        || originalFileName.toLowerCase().endsWith(".png")) {
                    imagePreview.setSrc(fileUrl);
                    imagePreview.setVisible(true);
                    pdfViewer.setVisible(false);
                } else if (originalFileName.toLowerCase().endsWith(".pdf")) {
                    pdfViewer.setSrc(fileUrl);
                    pdfViewer.setVisible(true);
                    imagePreview.setVisible(false);
                }

                deleteFileButton.setVisible(true);

                Notification.show("File uploaded successfully", 3000, Notification.Position.TOP_CENTER);

            } catch (IOException e) {
                Notification.show("Upload failed: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        vl22.add(upload,deleteFileButton);
        hl2.add(vl21,vl22, imagePreview, pdfViewer);
        add(hl2);

        HorizontalLayout hl3 = new HorizontalLayout();
        hl3.setSpacing(true);
        project.setLabel("Project");
        project.setWidth("min-content");
        project.getElement().getStyle().set("margin-left","15px");
//        SetProjectData(project);
        populateProjectData();

        hl3.add(project);
        add(hl3);

        configureBinder();


        if (transaction.getId() != null) {
            delete.setVisible(true);
        } else {
            delete.setVisible(false);
        }

        HorizontalLayout hl4 = new HorizontalLayout();
        hl4.setSpacing(true);
        hl4.setWidth("min-content");
        hl4.getElement().getStyle().set("margin-left","15px");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        HorizontalLayout hl41 = new HorizontalLayout(save,cancel, delete);
        hl4.add(hl41);
//        add(formLayout, buttons);
        add(hl4);
    }

    private void configureBinder() {
        binder = new BeanValidationBinder<>(Transaction.class);

        binder.forField(amount)
                .withConverter(
                        money -> money != null ? new BigDecimal(money.getAmount()) : null,
                        bigDecimal -> bigDecimal != null ? new Money(bigDecimal.toString(), amount.getCurrency().getValue()) : null,
                        "Invalid amount format"
                )
                .bind(
                        Transaction::getAmount,
                        (transaction, bigDecimal) -> {
                            transaction.setAmount(bigDecimal);
                            transaction.setCurrency(amount.getCurrency().getValue()); // Ensures currency is also set
                        }
                );
        amount.getCurrency().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                btw.getCurrency().setValue(event.getValue()); // Update btw currency to match amount
            }
            if (transaction != null) {
                transaction.setCurrency(event.getValue()); // Update currency whenever it changes
            }
        });

        binder.forField(btw)
                .withConverter(
                        // Convert Money to BigDecimal
                        money -> money != null ? new BigDecimal(money.getAmount()) : null,
                        // Convert BigDecimal back to Money
                        bigDecimal -> bigDecimal != null ? new Money(bigDecimal.toString(), "SRD") : null,
                        "Invalid BTW format"
                )
                .bind(Transaction::getBtw, (transaction, bigDecimal) -> transaction.setBtw(bigDecimal));

        binder.forField(category)
                .withConverter(
                        category -> category != null ? category.name : null, // Convert CategorieItems to String
                        string -> string != null ? new CategorieItems(string) : null, // Convert String back to CategorieItems
                        "Invalid category format"
                )
                .bind(Transaction::getCategory, (transaction, category) -> transaction.setCategory(category));

        binder.forField(dagboek)
                .withConverter(
                        dagboek -> dagboek != null ? dagboek.name : null,
                        string -> string != null ? new Dagboek(string) : null,
                        "Invalid Dagboek"
                )
                .bind(Transaction::getDagboek, (transaction, dagboek) -> transaction.setDagboek(dagboek));
        binder.forField(project)
                .bind(Transaction::getProject, Transaction::setProject);


        binder.forField(transactionTypeRadio)
                .bind(Transaction::getTransactionType, Transaction::setTransactionType);

        binder.bindInstanceFields(this);
    }
    //TODO: implement delete via RESTAPI
    private void deleteFile() {
        if (uploadedFilePath == null || uploadedFilePath.isEmpty()) {
            Notification.show("No file to delete.", 3000, Notification.Position.BOTTOM_START);
            return;
        }

        File file = new File(uploadedFilePath);
        if (file.exists() && file.delete()) {
            Notification.show("File deleted successfully.", 3000, Notification.Position.BOTTOM_START);
            uploadedFilePath = null;
            imagePreview.setVisible(false);
            pdfViewer.setVisible(false);
            deleteFileButton.setVisible(false);

            // Optional: remove from transaction if already set
            transaction.setFilePath(null);
        } else {
            Notification.show("Failed to delete file.", 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
//        String fileName = new File(uploadedFilePath).getName(); // Extract just the filename
//        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
//        String deleteUrl = "/uploads/" + encodedFileName;
//
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://localhost:8080" + deleteUrl)) // Full URL
//                    .DELETE()
//                    .build();
//
//            HttpResponse<String> response = HttpClient.newHttpClient()
//                    .send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                Notification.show("File deleted successfully.", 3000, Notification.Position.BOTTOM_START);
//                uploadedFilePath = null;
//                imagePreview.setVisible(false);
//                pdfViewer.setVisible(false);
//            } else {
//                Notification.show("Failed to delete file: " + response.body(), 5000, Notification.Position.BOTTOM_START)
//                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
//            }
//        } catch (Exception e) {
//            Notification.show("Error deleting file: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//        }
    }

    private void populateProjectData() {
        List<Project> projects = projectService.list(Pageable.unpaged()).getContent(); // Fetch all projects
        project.setItems(projects);
        project.setItemLabelGenerator(Project::getName); // Display project names in ComboBox
    }

    private void setCategoryData(ComboBox<CategorieItems> comboBox) {
        List<CategorieItems> categorieItems = new ArrayList<>();
        categorieItems.add(new CategorieItems("Bankkosten"));
        categorieItems.add(new CategorieItems("Belastingen"));
        categorieItems.add(new CategorieItems("Diensten/Projecten"));
        categorieItems.add(new CategorieItems("Huur en Nutsvoorzieningen"));
        categorieItems.add(new CategorieItems("Kantoorbenodigdheden"));
        categorieItems.add(new CategorieItems("Kruispost"));
        categorieItems.add(new CategorieItems("Lening"));
        categorieItems.add(new CategorieItems("Marketing"));
        categorieItems.add(new CategorieItems("Materiaalkosten"));
        categorieItems.add(new CategorieItems("Onderhoud & Reparatie"));
        categorieItems.add(new CategorieItems("Overig"));
        categorieItems.add(new CategorieItems("Salarissen en Lonen"));
        categorieItems.add(new CategorieItems("Subcontractors"));
        categorieItems.add(new CategorieItems("Training en Opleiding"));
        categorieItems.add(new CategorieItems("Verhuur"));
        categorieItems.add(new CategorieItems("Vervoer en Transport"));
        categorieItems.add(new CategorieItems("Verzekeringen"));

        comboBox.setItems(categorieItems);
        comboBox.setItemLabelGenerator(CategorieItems::name);
    }
    private void setDagboekData(ComboBox<Dagboek> comboBox) {
        List<Dagboek> dagboek = new ArrayList<>();
        dagboek.add(new Dagboek("Kas"));
        dagboek.add(new Dagboek("Bank"));

        comboBox.setItems(dagboek);
        comboBox.setItemLabelGenerator(Dagboek::name);

    }
    private void setLenerData(ComboBox<Lener> comboBox) {
        List<Lener> lener = new ArrayList<>();
        lener.add(new Lener("Anthony"));
        lener.add(new Lener("Gian"));
        lener.add(new Lener("Sirano"));
        lener.add(new Lener("Jeany"));

        comboBox.setItems(lener);
        comboBox.setItemLabelGenerator(Lener::name);
    }

    private void populateForm() {
        if (transaction.getId() == null) {
            // Reset form fields for new entries
            binder.readBean(null);
            transactionTypeRadio.setValue(TransactionType.CREDIT);
            dagboek.clear();
            date.setValue(LocalDate.now());
            amount.clear();
            description.clear();
            btw.setValue(new Money("0.00", "SRD"));
            category.clear();
            project.clear();
            uploadedFilePath = null;
            imagePreview.setVisible(false);
            pdfViewer.setVisible(false);
        } else {
            binder.readBean(transaction);
            amount.getCurrency().setValue(transaction.getCurrency());
            uploadedFilePath = transaction.getFilePath();
            if (uploadedFilePath != null) {
                String fileUrl = "/uploads/" + new File(uploadedFilePath).getName(); // Use HTTP URL

                if (uploadedFilePath.endsWith(".jpg")) {
                    imagePreview.setSrc(fileUrl);
                    imagePreview.setVisible(true);
                    pdfViewer.setVisible(false);
                } else if (uploadedFilePath.endsWith(".pdf")) {
                    pdfViewer.setSrc(fileUrl);
                    pdfViewer.setVisible(true);
                    imagePreview.setVisible(false);
                }
                deleteFileButton.setVisible(true);
            } else {
                deleteFileButton.setVisible(false);
            }
            if (transaction.getProject() != null) {
                project.setValue(transaction.getProject());
            }

        }
    }

    private void save() {
        try {
            // Bind UI fields to the transaction object
            binder.writeBean(transaction);

            boolean isNew = (transaction.getId() == null);

            // Step 1: Persist transaction to get the ID (if new)
            if (isNew) {
                transactionService.create(transaction);
                Notification.show("Transaction created");
            } else {
                transactionService.update(transaction);
                Notification.show("Transaction updated");
            }

            // Step 2: Handle uploaded file renaming
            if (uploadedFilePath != null && !uploadedFilePath.isEmpty()) {
                Long transactionId = transaction.getId(); // must exist now
                if (transactionId != null) {
                    File originalFile = new File(uploadedFilePath);
                    if (originalFile.exists()) {
                        // Get the file extension
                        String extension = "";
                        int dotIndex = uploadedFilePath.lastIndexOf('.');
                        if (dotIndex > 0 && dotIndex < uploadedFilePath.length() - 1) {
                            extension = uploadedFilePath.substring(dotIndex + 1);
                        }

                        // Create target file path
                        String targetFileName = transactionId + (extension.isEmpty() ? "" : "." + extension);
                        File targetFile = new File(settingsService.getUploadDirectory(), targetFileName);

                        if (!originalFile.getName().equals(targetFileName)) {
                            try {
                                Files.move(originalFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                uploadedFilePath = targetFile.getAbsolutePath();
                                transaction.setFilePath(uploadedFilePath);
                                transactionService.update(transaction); // Update path in DB
                            } catch (IOException ioException) {
                                Notification.show("Error renaming uploaded file: " + ioException.getMessage(),
                                                5000, Notification.Position.BOTTOM_START)
                                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            }
                        }
                    } else {
                        Notification.show("Uploaded file not found on disk.", 4000, Notification.Position.BOTTOM_START);
                    }
                } else {
                    Notification.show("Transaction ID not available for file rename.", 4000, Notification.Position.BOTTOM_START);
                }
            }

            // Step 3: Navigate away
            String route = "transactions";
            List<String> queryParams = new ArrayList<>();
            try {
                if (extraFilterValue != null && !extraFilterValue.isEmpty()) {
                    queryParams.add("extraFilter=" + URLEncoder.encode(extraFilterValue, StandardCharsets.UTF_8.name()));
                }
                if (categoryFilterValue != null && !categoryFilterValue.isEmpty()) {
                    queryParams.add("categoryFilter=" + URLEncoder.encode(categoryFilterValue, StandardCharsets.UTF_8.name()));
                }
                if (descriptionFilterValue != null && !descriptionFilterValue.isEmpty()) {
                    queryParams.add("descriptionFilter=" + URLEncoder.encode(descriptionFilterValue, StandardCharsets.UTF_8.name()));
                }
                if (startDateValue != null && !startDateValue.isEmpty()) {
                    queryParams.add("startDate=" + URLEncoder.encode(startDateValue, StandardCharsets.UTF_8.name()));
                }
                if (endDateValue != null && !endDateValue.isEmpty()) {
                    queryParams.add("endDate=" + URLEncoder.encode(endDateValue, StandardCharsets.UTF_8.name()));
                }
                if (typeValue != null && !typeValue.isEmpty()) {
                    queryParams.add("type=" + URLEncoder.encode(typeValue, StandardCharsets.UTF_8.name()));
                }
                if (projectIdValue != null && !projectIdValue.isEmpty()) {
                    queryParams.add("projectId=" + URLEncoder.encode(projectIdValue, StandardCharsets.UTF_8.name()));
                }
                if (dagboekValue != null && !dagboekValue.isEmpty()) {
                    queryParams.add("dagboek=" + URLEncoder.encode(dagboekValue, StandardCharsets.UTF_8.name()));
                }
            } catch (Exception e) {
                // Handle encoding exception if necessary, perhaps log it
                Notification.show("Error encoding filter parameters for navigation", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            if (!queryParams.isEmpty()) {
                route += "?" + String.join("&", queryParams);
            }
            UI.getCurrent().navigate(route);

        } catch (ValidationException e) {
            Notification.show("Validation error: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (ObjectOptimisticLockingFailureException e) {
            Notification.show("Conflict: Transaction was modified by another user", 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }


    private void cancel() {
        String route = "transactions";
        List<String> queryParams = new ArrayList<>();

        try {
            if (extraFilterValue != null && !extraFilterValue.isEmpty()) {
                queryParams.add("extraFilter=" + URLEncoder.encode(extraFilterValue, StandardCharsets.UTF_8.name()));
            }
            if (categoryFilterValue != null && !categoryFilterValue.isEmpty()) {
                queryParams.add("categoryFilter=" + URLEncoder.encode(categoryFilterValue, StandardCharsets.UTF_8.name()));
            }
            if (descriptionFilterValue != null && !descriptionFilterValue.isEmpty()) {
                queryParams.add("descriptionFilter=" + URLEncoder.encode(descriptionFilterValue, StandardCharsets.UTF_8.name()));
            }
            if (startDateValue != null && !startDateValue.isEmpty()) {
                queryParams.add("startDate=" + URLEncoder.encode(startDateValue, StandardCharsets.UTF_8.name()));
            }
            if (endDateValue != null && !endDateValue.isEmpty()) {
                queryParams.add("endDate=" + URLEncoder.encode(endDateValue, StandardCharsets.UTF_8.name()));
            }
            if (typeValue != null && !typeValue.isEmpty()) {
                queryParams.add("type=" + URLEncoder.encode(typeValue, StandardCharsets.UTF_8.name()));
            }
            if (projectIdValue != null && !projectIdValue.isEmpty()) {
                queryParams.add("projectId=" + URLEncoder.encode(projectIdValue, StandardCharsets.UTF_8.name()));
            }
            if (dagboekValue != null && !dagboekValue.isEmpty()) {
                queryParams.add("dagboek=" + URLEncoder.encode(dagboekValue, StandardCharsets.UTF_8.name()));
            }
        } catch (Exception e) {
            // Handle encoding exception if necessary, perhaps log it
            Notification.show("Error encoding filter parameters", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        if (!queryParams.isEmpty()) {
            route += "?" + String.join("&", queryParams);
        }
        UI.getCurrent().navigate(route);
    }

    private void deleteTransaction() {
        transactionService.delete(transaction.getId());
        UI.getCurrent().navigate(TransactionListView.class);
    }
    private void handleError(Exception e) {
        Notification.show("An error occurred: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void updatePageTitle() {
        if (transaction.getId() == null) {
            UI.getCurrent().getPage().setTitle("New Transaction");
        } else {
            UI.getCurrent().getPage().setTitle("Edit Transaction");
        }
    }

    public record CategorieItems(String name) {
    }

    public record Lener(String name) {
    }

    public record Dagboek(String name) {
    }
}
