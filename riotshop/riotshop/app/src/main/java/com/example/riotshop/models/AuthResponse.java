package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("user")
    private UserResponse user;
    
    @SerializedName("token")
    private String token;
    
    @SerializedName("expiresAt")
    private String expiresAt;

    public UserResponse getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}

