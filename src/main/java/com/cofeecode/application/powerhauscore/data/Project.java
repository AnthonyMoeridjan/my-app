package com.cofeecode.application.powerhauscore.data;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Project extends AbstractEntity{
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
}
