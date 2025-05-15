package com.cofeecode.application.powerhauscore.data;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "workers")
public class Worker extends AbstractEntity{

    @Column(name="first_name", length=100)
    private String firstName;

    @Column(name="last_name", length=100)
    private String lastName;

    @Column(name="phone_number", length=100)
    private String phoneNumber;

    @Column(name="email", length=150)
    private String email;

    @Column(name="address", length=255)
    private String address;

    @Column(name="bank_name", length=150)
    private String bankName;

    @Column(name="bank_account_number", length=100)
    private String bankAccountNumber;

    @Column(name="role", length=50)
    private String role;

    @Column(name="uur_loon", precision = 38, scale = 2)
    private BigDecimal uurloon;

    @Column(nullable = false)
    private boolean active;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public BigDecimal getUurloon() {
        return uurloon;
    }

    public void setUurloon(BigDecimal uurloon) {
        this.uurloon = uurloon;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}