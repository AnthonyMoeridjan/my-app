package com.cofeecode.application.powerhauscore.dto;

import com.cofeecode.application.powerhauscore.data.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequestDTO {

    @Size(max = 45, message = "Dagboek cannot exceed 45 characters")
    private String dagboek;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Category is required")
    @Size(max = 45, message = "Category cannot exceed 45 characters")
    private String category;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @NotNull(message = "Amount is required")
    // The constraint on amount might need to be adjusted based on business rules
    // e.g., if amount should always be positive and type denotes debit/credit.
    private BigDecimal amount;

    @DecimalMin(value = "0.00", inclusive = true, message = "BTW cannot be negative")
    private BigDecimal btw;

    @Size(max = 45, message = "Currency cannot exceed 45 characters")
    private String currency;

    private Long projectId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Size(max = 500, message = "File path cannot exceed 500 characters")
    private String filePath;

    @Size(max = 45, message = "Extra info cannot exceed 45 characters")
    private String extra;

    // Getters and Setters
    public String getDagboek() { return dagboek; }
    public void setDagboek(String dagboek) { this.dagboek = dagboek; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBtw() { return btw; }
    public void setBtw(BigDecimal btw) { this.btw = btw; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getExtra() { return extra; }
    public void setExtra(String extra) { this.extra = extra; }
}
