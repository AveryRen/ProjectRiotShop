package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class ProductTemplate {
    @SerializedName("templateId")
    private int templateId;
    
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
    
    @SerializedName("gameName")
    private String gameName; // Backend có thể trả về gameName trực tiếp
    
    @SerializedName("gameType")
    private GameType gameType;
    
    @SerializedName("inventory")
    private Inventory inventory;

    // Getters
    public int getTemplateId() {
        return templateId;
    }

    public int getGameId() {
        return gameId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public String getTagRank() {
        return tagRank;
    }

    public String getTagSkins() {
        return tagSkins;
    }

    public String getTagCollection() {
        return tagCollection;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getGameName() {
        if (gameName != null && !gameName.isEmpty()) {
            return gameName;
        }
        if (gameType != null) {
            return gameType.getName();
        }
        return "";
    }

    public GameType getGameType() {
        return gameType;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    // Inner class for Inventory
    public static class Inventory {
        @SerializedName("packageId")
        private int packageId;
        
        @SerializedName("quantityAvailable")
        private int quantityAvailable;
        
        @SerializedName("price")
        private double price;
        
        @SerializedName("lastUpdated")
        private String lastUpdated;
        
        public int getPackageId() {
            return packageId;
        }
        
        public int getQuantityAvailable() {
            return quantityAvailable;
        }
        
        public double getPrice() {
            return price;
        }
        
        public String getLastUpdated() {
            return lastUpdated;
        }
    }
}

