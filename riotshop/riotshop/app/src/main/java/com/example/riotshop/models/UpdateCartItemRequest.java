package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class UpdateCartItemRequest {
    @SerializedName("quantity")
    private int quantity;

    public UpdateCartItemRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}

