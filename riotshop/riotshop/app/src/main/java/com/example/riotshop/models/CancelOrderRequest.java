package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class CancelOrderRequest {
    @SerializedName("reason")
    private String reason;

    public CancelOrderRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}

