package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class UpdateAddressRequest {
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
    private Boolean isDefault;

    public UpdateAddressRequest(String fullName, String phoneNumber, String addressLine, 
                                String city, String district, String ward, Boolean isDefault) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.addressLine = addressLine;
        this.city = city;
        this.district = district;
        this.ward = ward;
        this.isDefault = isDefault;
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

    public Boolean getIsDefault() {
        return isDefault;
    }
}

