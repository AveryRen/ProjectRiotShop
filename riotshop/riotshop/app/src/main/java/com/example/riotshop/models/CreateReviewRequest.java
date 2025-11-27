package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class CreateReviewRequest {
    @SerializedName("templateId")
    private int templateId;
    
    @SerializedName("rating")
    private int rating;
    
    @SerializedName("comment")
    private String comment;

    public CreateReviewRequest(int templateId, int rating, String comment) {
        this.templateId = templateId;
        this.rating = rating;
        this.comment = comment;
    }

    public int getTemplateId() {
        return templateId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}

