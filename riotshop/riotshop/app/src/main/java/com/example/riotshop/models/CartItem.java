package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("cartItemId")
    private int cartItemId;
    
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("templateId")
    private int templateId;
    
    @SerializedName("quantity")
    private int quantity;
    
    @SerializedName("productTemplate")
    private ProductTemplate productTemplate;

    // Getters
    public int getCartItemId() {
        return cartItemId;
    }

    public int getUserId() {
        return userId;
    }

    public int getTemplateId() {
        return templateId;
    }

    public int getQuantity() {
        return quantity;
    }

    public ProductTemplate getProductTemplate() {
        return productTemplate;
    }
}
