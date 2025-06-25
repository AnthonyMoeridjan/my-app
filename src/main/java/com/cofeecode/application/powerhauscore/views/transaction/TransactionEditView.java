package com.cofeecode.application.powerhauscore.views.transaction;

import com.cofeecode.application.powerhauscore.customfield.Money;
import com.cofeecode.application.powerhauscore.customfield.PriceField;
import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.Transaction;
import com.cofeecode.application.powerhauscore.data.TransactionType;
import com.cofeecode.application.powerhauscore.security.AuthenticatedUser;
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
import java.nio.file.Files; // Re-adding for Files.deleteIfExists and Files.copy
import java.nio.file.Path;
import java.nio.file.Paths;

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
    // private static final String UPLOAD_DIRECTORY = "D:/Java Projects/my-app/uploads"; // Removed

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
    private final AuthenticatedUser authenticatedUser;
    private Transaction transaction;
    private BeanValidationBinder<Transaction> binder;
    private boolean isEditMode = false;

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
    private final Button editButton = new Button("Edit");

    // --- Photo Upload Fields ---
    private FileBuffer photoFileBuffer;
    private Upload photoUpload;
    private Image photoPreview;
    private Button downloadPhotoButton;
    private Button deletePhotoButton;
    private String tempUploadedPhotoPath; // To store path of newly uploaded file before saving transaction
    private String currentPhotoFileName; // To store filename of existing photo for the transaction
    // --- End Photo Upload Fields ---

    public TransactionEditView(TransactionService transactionService, ProjectService projectService, SettingsService settingsService, AuthenticatedUser authenticatedUser) {
        this.transactionService = transactionService;
        this.projectService = projectService;
        this.settingsService = settingsService;
        this.authenticatedUser = authenticatedUser;
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
            isEditMode = true; // New transactions start in edit mode
        }
        createForm();
        updatePageTitle();
        populateForm(); // This will also call setFieldsReadOnly based on isEditMode

        // Delete button visibility is handled in createForm based on transaction.getId()
        // All other button states are handled by updateButtonStates()
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean canEdit = authenticatedUser.get().map(user -> user.getRoles().stream()
                        .anyMatch(role -> role == com.cofeecode.application.powerhauscore.data.Role.ADMIN ||
                                role == com.cofeecode.application.powerhauscore.data.Role.HR))
                .orElse(false);

        if (isEditMode) {
            editButton.setVisible(false);
            save.setVisible(canEdit); // If in edit mode, save is possible if user has edit roles
            cancel.setVisible(true); // Always show cancel when in edit mode
        } else { // View mode
            editButton.setVisible(canEdit);
            save.setVisible(false);
            // Cancel button in view mode can act as a "Back" button.
            // Or, it could be hidden if no "Back" action is desired when just viewing.
            // For consistency with its behavior (navigation), let's keep it visible.
            cancel.setVisible(true);
        }
        // Delete button visibility is handled by this method now.
        boolean isAdmin = authenticatedUser.get().map(user -> user.getRoles().stream()
                        .anyMatch(role -> role == com.cofeecode.application.powerhauscore.data.Role.ADMIN))
                .orElse(false);
        delete.setVisible(transaction.getId() != null && isAdmin);
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

        // Initialize editButton themes and click listener here as it's part of hl1 now
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> enterEditMode());
        // Edit button is initially invisible, its state is managed by updateButtonStates
        editButton.setVisible(false);


        Paragraph textSmall = new Paragraph("SRD 0.00 $ 0.00 Euro 0.00");
        textSmall.setWidth("20em");
        textSmall.getStyle().set("font-size", "var(--lumo-font-size-xs)");

        hl1.add(transactionTypeRadio, editButton); // Add editButton to hl1
        hl1.setFlexGrow(1, transactionTypeRadio); // Allow radio group to take available space
        hl1.setAlignItems(FlexComponent.Alignment.BASELINE); // Align items to baseline

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

        // --- Photo Upload UI ---
        VerticalLayout photoLayout = new VerticalLayout();
        photoLayout.setWidth("min-content");
        photoLayout.setSpacing(true);

        photoFileBuffer = new FileBuffer();
        photoUpload = new Upload(photoFileBuffer);
        photoUpload.setAcceptedFileTypes("image/jpeg", "image/png");
        photoUpload.setMaxFiles(1);
        Span photoLabel = new Span("Transaction Photo");

        photoPreview = new Image();
        photoPreview.setMaxWidth("300px");
        photoPreview.setMaxHeight("300px");
        photoPreview.setVisible(false);

        downloadPhotoButton = new Button("Download Photo");
        downloadPhotoButton.setVisible(false);
        deletePhotoButton = new Button("Delete Photo");
        deletePhotoButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deletePhotoButton.setVisible(false);

        downloadPhotoButton.addClickListener(e -> {
            if (currentPhotoFileName != null && !currentPhotoFileName.isEmpty()) {
                UI.getCurrent().getPage().open("/files/download/" + currentPhotoFileName, "_blank");
            } else {
                Notification.show("No photo to download.", 3000, Notification.Position.MIDDLE);
            }
        });

        deletePhotoButton.addClickListener(e -> deletePhoto());

        HorizontalLayout photoButtons = new HorizontalLayout(downloadPhotoButton, deletePhotoButton);
        // photoButtons.setVisible(false); // Visibility of individual buttons is handled by updatePhotoComponentVisibility

        photoLayout.add(photoLabel, photoUpload, photoPreview, photoButtons);

        // --- Photo Upload Event Handling ---
        photoUpload.addSucceededListener(event -> {
            try (InputStream inputStream = photoFileBuffer.getInputStream()) {
                File tempDir = Files.createTempDirectory("temp-upload-").toFile();
                File tempFile = new File(tempDir, event.getFileName());
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                tempUploadedPhotoPath = tempFile.getAbsolutePath(); // Store path for later processing in save()
                photoPreview.setSrc("file:///" + tempUploadedPhotoPath); // Temporary local preview
                photoPreview.setVisible(true);
                Notification.show("Photo '" + event.getFileName() + "' uploaded. Save transaction to confirm.", 3000, Notification.Position.MIDDLE);
                // Do not show download/delete for a temp file that isn't yet persisted with the transaction
                downloadPhotoButton.setVisible(false);
                deletePhotoButton.setVisible(false);
                photoButtons.setVisible(false);
            } catch (IOException e) {
                Notification.show("Photo upload failed: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                tempUploadedPhotoPath = null;
            }
        });

        photoUpload.addFileRejectedListener(event -> {
            Notification.show("Photo rejected: " + event.getErrorMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        photoUpload.addFailedListener(event -> {
            Notification.show("Photo upload failed: " + event.getReason().getMessage(), 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            tempUploadedPhotoPath = null;
        });
        // --- End Photo Upload Event Handling ---

        // --- End Photo Upload UI ---

        hl2.add(vl21, photoLayout); // Add vl21 and photoLayout
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

        // Delete button visibility is now handled in updateButtonStates()

        HorizontalLayout hl4 = new HorizontalLayout();
        hl4.setSpacing(true);
        hl4.setWidth("min-content");
        hl4.getElement().getStyle().set("margin-left","15px");

        // editButton is now part of hl1
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);

        // Initial button visibility
        // editButton.setVisible(false); // Now handled in its new location
        save.setVisible(false);
        cancel.setVisible(false);


        HorizontalLayout hl41 = new HorizontalLayout(save, cancel, delete); // Removed editButton from here
        hl4.add(hl41);
//        add(formLayout, buttons);
        add(hl4);
    }

    private void enterEditMode() {
        isEditMode = true;
        setFieldsReadOnly(false);
        updateButtonStates();
        // Delete button visibility remains unchanged by mode switch, only by existence of transaction.
    }

    private void setFieldsReadOnly(boolean readOnly) {
        date.setReadOnly(readOnly);
        amount.setReadOnly(readOnly);
        description.setReadOnly(readOnly);
        btw.setReadOnly(readOnly);
        dagboek.setReadOnly(readOnly);
        category.setReadOnly(readOnly);
        lener.setReadOnly(readOnly);
        project.setReadOnly(readOnly);
        transactionTypeRadio.setReadOnly(readOnly);
        photoUpload.setVisible(!readOnly);

        // Visibility of download/delete buttons is handled by updatePhotoComponentVisibility
        // based on currentPhotoFileName and not directly by readOnly state here,
        // but upload component itself should be hidden in readOnly mode.


        // For PriceField, we need to make sure the currency selector is also read-only.
        // PriceField itself might handle its internal components, but if not, explicit control is needed.
        // Assuming PriceField's setReadOnly(true) correctly makes the amount and currency parts read-only.
        // If PriceField does not cascade readOnly to its currency ComboBox, we would do:
        // amount.getCurrency().setReadOnly(readOnly);
        // btw.getCurrency().setReadOnly(readOnly);
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

    // Removed deleteFile() method
    // private void deleteFile() { ... }

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
            currentPhotoFileName = null;
            tempUploadedPhotoPath = null;
            updatePhotoComponentVisibility();
        } else {
            binder.readBean(transaction);
            amount.getCurrency().setValue(transaction.getCurrency());
            currentPhotoFileName = transaction.getFilePath(); // Use existing filePath for photo
            tempUploadedPhotoPath = null; // Clear any pending new upload
            updatePhotoComponentVisibility();

            if (transaction.getProject() != null) {
                project.setValue(transaction.getProject());
            }
        }
        setFieldsReadOnly(!isEditMode); // Ensure fields read-only state is set based on current mode
    }

    private void deletePhoto() {
        if (currentPhotoFileName == null || currentPhotoFileName.isEmpty()) {
            Notification.show("No photo to delete.", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Confirm dialog can be added here for better UX
        // For now, direct delete:

        try {
            Path photoPath = Paths.get(settingsService.getUploadDirectory(), currentPhotoFileName);
            Files.deleteIfExists(photoPath);

            transaction.setFilePath(null);
            transactionService.update(transaction); // Save the transaction with null filePath

            currentPhotoFileName = null; // Clear current filename
            tempUploadedPhotoPath = null; // Clear any pending upload
            updatePhotoComponentVisibility(); // Update UI

            Notification.show("Photo deleted successfully.", 3000, Notification.Position.MIDDLE);
        } catch (IOException e) {
            Notification.show("Error deleting photo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updatePhotoComponentVisibility() {
        boolean photoExists = currentPhotoFileName != null && !currentPhotoFileName.isEmpty();
        photoPreview.setVisible(photoExists);
        downloadPhotoButton.setVisible(photoExists);
        deletePhotoButton.setVisible(photoExists);
        // photoButtons layout visibility could also be controlled here if it's a separate container for both buttons.
        // For now, individual visibility is fine.
        if (photoExists) {
            photoPreview.setSrc("/files/view/" + currentPhotoFileName);
        } else {
            photoPreview.setSrc(null); // Clear src if no photo
        }
    }

    private void save() {
        try {
            binder.writeBean(transaction); // Write other fields first

            // Handle photo file processing
            if (tempUploadedPhotoPath != null && !tempUploadedPhotoPath.isEmpty()) {
                // New photo was uploaded or existing one replaced
                File tempFile = new File(tempUploadedPhotoPath);
                if (tempFile.exists()) {
                    // Persist transaction first if it's new, to get an ID for the filename
                    if (transaction.getId() == null) {
                        transactionService.create(transaction); // Create to get ID
                        Notification.show("Transaction created. Saving photo...");
                    }

                    // Delete old photo if exists and filename is changing or was present
                    if (currentPhotoFileName != null && !currentPhotoFileName.isEmpty()) {
                        try {
                            Path oldPhotoPath = Paths.get(settingsService.getUploadDirectory(), currentPhotoFileName);
                            Files.deleteIfExists(oldPhotoPath);
                        } catch (IOException e) {
                            Notification.show("Could not delete old photo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    }

                    String originalFileName = tempFile.getName();
                    String extension = "";
                    int dotIndex = originalFileName.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                        extension = originalFileName.substring(dotIndex); // includes the dot e.g. ".jpg"
                    }
                    String newFileName = transaction.getId() + extension;
                    Path targetPath = Paths.get(settingsService.getUploadDirectory(), newFileName);

                    try {
                        Files.copy(tempFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        transaction.setFilePath(newFileName); // Save new file name
                        currentPhotoFileName = newFileName; // Update current filename
                        tempUploadedPhotoPath = null; // Clear temp path
                        if(tempFile.exists()) tempFile.delete(); // Delete temp file after copy
                        if(tempFile.getParentFile().exists()) tempFile.getParentFile().delete(); // Delete temp directory

                    } catch (IOException e) {
                        Notification.show("Error saving photo: " + e.getMessage(), 5000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        transaction.setFilePath(currentPhotoFileName); // Revert to old filename if save fails
                    }
                }
            }
            // If no new photo was uploaded, transaction.getFilePath() (currentPhotoFileName) remains as is or null

            transactionService.update(transaction); // Update transaction with new/old/null filePath
            Notification.show(transaction.getId() == null || (tempUploadedPhotoPath != null && !new File(tempUploadedPhotoPath).exists()) ? "Transaction updated." : "Transaction and photo updated.");


            // Exit edit mode
            isEditMode = false;
            updatePhotoComponentVisibility(); // Update preview based on saved state
            setFieldsReadOnly(true);
            updateButtonStates();

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
        // Reset edit mode and form state
        isEditMode = false;
        populateForm(); // This will re-bind the original bean and calls setFieldsReadOnly

        // Update button visibility
        updateButtonStates();

        // Navigate away
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
