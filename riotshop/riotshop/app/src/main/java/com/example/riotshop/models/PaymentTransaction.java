package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class PaymentTransaction {
    @SerializedName("transactionId")
    private int transactionId;
    
    @SerializedName("amount")
    private double amount;
    
    @SerializedName("currency")
    private String currency;
    
    @SerializedName("status")
    private String status; // pending, succeeded, failed, canceled
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("completedAt")
    private String completedAt;
    
    @SerializedName("failureReason")
    private String failureReason;

    // Getters
    public int getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    // Setters
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}

