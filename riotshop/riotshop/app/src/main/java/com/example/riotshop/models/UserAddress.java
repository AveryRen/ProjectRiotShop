package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class UserAddress {
    @SerializedName("addressId")
    private int addressId;
    
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("addressLine")
    private String addressLine;
    
    @SerializedName("city")
    private String city;
    
    @SerializedName("district")
    private String district;
    
    @SerializedName("ward")
    private String ward;
    
    @SerializedName("isDefault")
    private boolean isDefault;
    
    @SerializedName("createdAt")
    private String createdAt;

    public int getAddressId() {
        return addressId;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getWard() {
        return ward;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

