package com.cofeecode.application.powerhauscore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectRequestDTO {

    @NotBlank(message = "Project name cannot be blank")
    private String name;

    private String description;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal budget;

    @NotBlank(message = "Status cannot be blank")
    private String status;

    @NotBlank(message = "Client cannot be blank")
    private String client;

    private String manager;

    private String location;

    private boolean isPriority;

    public ProjectRequestDTO() {
    }

    public ProjectRequestDTO(String name, String description, LocalDate startDate, LocalDate endDate, BigDecimal budget, String status, String client, String manager, String location, boolean isPriority) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.status = status;
        this.client = client;
        this.manager = manager;
        this.location = location;
        this.isPriority = isPriority;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public boolean isPriority() { return isPriority; }
    public void setPriority(boolean priority) { isPriority = priority; }
}
