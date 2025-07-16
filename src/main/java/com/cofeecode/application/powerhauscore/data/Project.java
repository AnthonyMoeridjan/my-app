package com.cofeecode.application.powerhauscore.data;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Project extends AbstractEntity {
    //    @NotBlank
    private String name;  // Name of the project

    private String description;  // Detailed description of the project

    //    @NotNull
    private LocalDate startDate;  // Start date of the project

    private LocalDate endDate;  // End date of the project (can be null if ongoing)

    //    @NotNull
    private BigDecimal budget;  // Budget allocated for the project

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;  // Current status of the project

    private String client;  // Name of the client for whom the project is being executed

    private String manager;  // Name of the project manager

    private String location;  // Location where the project is being carried out

    private boolean isPriority;  // Indicates if the project is a priority

    private String quoteFile;
    private BigDecimal quoteAmount;
    private String invoiceFile;
    private BigDecimal invoiceAmount;

    @ElementCollection
    @CollectionTable(name = "project_status_timestamps", joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyColumn(name = "status")
    @Column(name = "timestamp")
    private Map<ProjectStatus, LocalDateTime> statusTimestamps = new HashMap<>();

    // Getters and Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public BigDecimal getBudget() {
        return budget;
    }
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
    public ProjectStatus getStatus() {
        return status;
    }
    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
    public String getClient() {
        return client;
    }
    public void setClient(String client) {
        this.client = client;
    }
    public String getManager() {
        return manager;
    }
    public void setManager(String manager) {
        this.manager = manager;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public boolean isPriority() {
        return isPriority;
    }
    public void setPriority(boolean isPriority) {
        this.isPriority = isPriority;
    }

    public String getQuoteFile() {
        return quoteFile;
    }

    public void setQuoteFile(String quoteFile) {
        this.quoteFile = quoteFile;
    }

    public BigDecimal getQuoteAmount() {
        return quoteAmount;
    }

    public void setQuoteAmount(BigDecimal quoteAmount) {
        this.quoteAmount = quoteAmount;
    }

    public String getInvoiceFile() {
        return invoiceFile;
    }

    public void setInvoiceFile(String invoiceFile) {
        this.invoiceFile = invoiceFile;
    }

    public BigDecimal getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(BigDecimal invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public Map<ProjectStatus, LocalDateTime> getStatusTimestamps() {
        return statusTimestamps;
    }

    public void setStatusTimestamps(Map<ProjectStatus, LocalDateTime> statusTimestamps) {
        this.statusTimestamps = statusTimestamps;
    }
}
