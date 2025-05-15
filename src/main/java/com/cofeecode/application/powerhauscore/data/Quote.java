package com.cofeecode.application.powerhauscore.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "quotes")
public class Quote extends AbstractEntity {

    @Column(unique = true, nullable = false)
    private String quoteNumber;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String client;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency;

    @Temporal(TemporalType.DATE)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private QuoteStatus status;

    @Column(length = 255)
    private String invoice;

    @Temporal(TemporalType.DATE)
    private LocalDate firstPaymentDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal firstPaymentAmount;

    @Column(length = 10)
    private String firstPaymentCurrency;

    @Temporal(TemporalType.DATE)
    private LocalDate lastPaymentDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal lastPaymentAmount;

    @Column(length = 10)
    private String lastPaymentCurrency;

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public void setQuoteNumber(String quoteNumber) {
        this.quoteNumber = quoteNumber;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public void setStatus(QuoteStatus status) {
        this.status = status;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public LocalDate getFirstPaymentDate() {
        return firstPaymentDate;
    }

    public void setFirstPaymentDate(LocalDate firstPaymentDate) {
        this.firstPaymentDate = firstPaymentDate;
    }

    public BigDecimal getFirstPaymentAmount() {
        return firstPaymentAmount;
    }

    public void setFirstPaymentAmount(BigDecimal firstPaymentAmount) {
        this.firstPaymentAmount = firstPaymentAmount;
    }

    public String getFirstPaymentCurrency() {
        return firstPaymentCurrency;
    }

    public void setFirstPaymentCurrency(String firstPaymentCurrency) {
        this.firstPaymentCurrency = firstPaymentCurrency;
    }

    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(LocalDate lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }

    public BigDecimal getLastPaymentAmount() {
        return lastPaymentAmount;
    }

    public void setLastPaymentAmount(BigDecimal lastPaymentAmount) {
        this.lastPaymentAmount = lastPaymentAmount;
    }

    public String getLastPaymentCurrency() {
        return lastPaymentCurrency;
    }

    public void setLastPaymentCurrency(String lastPaymentCurrency) {
        this.lastPaymentCurrency = lastPaymentCurrency;
    }
}
