package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class ConfirmPaymentRequest {
    @SerializedName("paymentIntentId")
    private String paymentIntentId;

    public ConfirmPaymentRequest(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }
}

