package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class AddToCartRequest {
    @SerializedName("templateId")
    private int templateId;
    
    @SerializedName("quantity")
    private int quantity;

    public AddToCartRequest(int templateId, int quantity) {
        this.templateId = templateId;
        this.quantity = quantity;
    }

    public int getTemplateId() {
        return templateId;
    }

    public int getQuantity() {
        return quantity;
    }
}

