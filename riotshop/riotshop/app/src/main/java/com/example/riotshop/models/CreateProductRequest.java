package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class CreateProductRequest {
    @SerializedName("gameId")
    private int gameId;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("basePrice")
    private double basePrice;
    
    @SerializedName("isFeatured")
    private boolean isFeatured;
    
    @SerializedName("tagRank")
    private String tagRank;
    
    @SerializedName("tagSkins")
    private String tagSkins;
    
    @SerializedName("tagCollection")
    private String tagCollection;
    
    @SerializedName("imageUrl")
    private String imageUrl;
    
    public CreateProductRequest(int gameId, String title, String description, double basePrice, 
                                boolean isFeatured, String tagRank, String tagSkins, String tagCollection, String imageUrl) {
        this.gameId = gameId;
        this.title = title;
        this.description = description;
        this.basePrice = basePrice;
        this.isFeatured = isFeatured;
        this.tagRank = tagRank;
        this.tagSkins = tagSkins;
        this.tagCollection = tagCollection;
        this.imageUrl = imageUrl;
    }
    
    // Getters
    public int getGameId() { return gameId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getBasePrice() { return basePrice; }
    public boolean isFeatured() { return isFeatured; }
    public String getTagRank() { return tagRank; }
    public String getTagSkins() { return tagSkins; }
    public String getTagCollection() { return tagCollection; }
    public String getImageUrl() { return imageUrl; }
}

