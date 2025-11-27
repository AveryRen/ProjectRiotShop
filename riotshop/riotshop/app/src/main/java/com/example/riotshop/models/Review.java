package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("reviewId")
    private int reviewId;
    
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("templateId")
    private int templateId;
    
    @SerializedName("rating")
    private int rating;
    
    @SerializedName("comment")
    private String comment;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("isApproved")
    private boolean isApproved;
    
    @SerializedName("username")
    private String username; // Backend trả về username trực tiếp
    
    @SerializedName("user")
    private User user; // Fallback nếu backend trả về object User
    
    @SerializedName("productTemplate")
    private ProductTemplate productTemplate;

    public int getReviewId() {
        return reviewId;
    }

    public int getUserId() {
        return userId;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public String getUsername() {
        if (username != null && !username.isEmpty()) {
            return username;
        }
        if (user != null) {
            return user.getUsername();
        }
        return "Người dùng";
    }

    public User getUser() {
        return user;
    }

    public ProductTemplate getProductTemplate() {
        return productTemplate;
    }
}

