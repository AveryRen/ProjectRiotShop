package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class Wishlist {
    @SerializedName("wishlistId")
    private int wishlistId;
    
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("templateId")
    private int templateId;
    
    @SerializedName("addedAt")
    private String addedAt;
    
    @SerializedName("productTemplate")
    private ProductTemplate productTemplate;

    public int getWishlistId() {
        return wishlistId;
    }

    public int getUserId() {
        return userId;
    }

    public int getTemplateId() {
        return templateId;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public ProductTemplate getProductTemplate() {
        return productTemplate;
    }
}

