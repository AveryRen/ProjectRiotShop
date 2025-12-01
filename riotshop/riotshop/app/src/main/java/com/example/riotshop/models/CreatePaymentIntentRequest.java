package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class CreatePaymentIntentRequest {
    @SerializedName("amount")
    private double amount;
    
    @SerializedName("currency")
    private String currency;

    public CreatePaymentIntentRequest(double amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

