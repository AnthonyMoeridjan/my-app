package com.cofeecode.application.powerhauscore.data;

import com.cofeecode.application.powerhauscore.security.Auditable;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transactions")
public class Transaction extends AbstractEntity implements Auditable {

    @Column(length = 45)
    private String dagboek;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(length = 45, nullable = false)
    private String category;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 38, scale = 2)
    private BigDecimal amount;

    @Column(precision = 38, scale = 2)
    private BigDecimal btw;

    @Column(length = 45)
    private String currency;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private TransactionType transactionType;

    @Column(length = 500) // Stores the file path instead of the actual file
    private String filePath;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(length = 45)
    private String extra;

    public String getDagboek() {
        return dagboek;
    }

    public void setDagboek(String dagboek) {this.dagboek = dagboek; }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBtw() {
        return btw;
    }

    public void setBtw(BigDecimal btw) {
        this.btw = btw;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getExtra() {
        return extra;
    }
    public void setExtra(String extra) {
        this.extra = extra;
    }
}
