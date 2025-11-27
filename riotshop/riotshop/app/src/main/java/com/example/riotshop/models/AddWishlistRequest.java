package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class AddWishlistRequest {
    @SerializedName("templateId")
    private int templateId;

    public AddWishlistRequest(int templateId) {
        this.templateId = templateId;
    }

    public int getTemplateId() {
        return templateId;
    }
}

