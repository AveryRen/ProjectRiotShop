package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("isAdmin")
    private boolean isAdmin;
    
    @SerializedName("balance")
    private double balance;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("avatarUrl")
    private String avatarUrl;
    
    @SerializedName("lastLogin")
    private String lastLogin;

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public double getBalance() {
        return balance;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getLastLogin() {
        return lastLogin;
    }
}

