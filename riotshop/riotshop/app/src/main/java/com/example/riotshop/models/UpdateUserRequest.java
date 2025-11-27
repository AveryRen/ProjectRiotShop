package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class UpdateUserRequest {
    @SerializedName("email")
    private String email;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("avatarUrl")
    private String avatarUrl;

    public UpdateUserRequest(String email, String fullName, String phoneNumber, String address, String avatarUrl) {
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.avatarUrl = avatarUrl;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }
}

