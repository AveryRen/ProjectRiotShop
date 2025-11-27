package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class UpdateProductRequest {
    @SerializedName("gameId")
    private Integer gameId;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("basePrice")
    private Double basePrice;
    
    @SerializedName("isFeatured")
    private Boolean isFeatured;
    
    @SerializedName("tagRank")
    private String tagRank;
    
    @SerializedName("tagSkins")
    private String tagSkins;
    
    @SerializedName("tagCollection")
    private String tagCollection;
    
    @SerializedName("imageUrl")
    private String imageUrl;
    
    // Getters and Setters
    public Integer getGameId() { return gameId; }
    public void setGameId(Integer gameId) { this.gameId = gameId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public String getTagRank() { return tagRank; }
    public void setTagRank(String tagRank) { this.tagRank = tagRank; }
    
    public String getTagSkins() { return tagSkins; }
    public void setTagSkins(String tagSkins) { this.tagSkins = tagSkins; }
    
    public String getTagCollection() { return tagCollection; }
    public void setTagCollection(String tagCollection) { this.tagCollection = tagCollection; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

