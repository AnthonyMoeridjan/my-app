package com.cofeecode.application.powerhauscore.data;

import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class Services extends AbstractEntity{

//    @NotBlank
    private String name;  // Name of the service

    private String description;  // Detailed description of the service

//    @NotNull
    private BigDecimal price;  // Price for the service

    private String category;  // Category of the service (e.g., Welding, Fabrication, Rental)

    private boolean available;  // Indicates if the service is currently available

    private String duration;  // Estimated duration of the service (e.g., "2 hours", "3 days")

    private String unit;  // Unit of measure (e.g., per hour, per job, per day)

    private String notes;  // Additional notes or details about the service

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
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public boolean isAvailable() {
        return available;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }
    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
