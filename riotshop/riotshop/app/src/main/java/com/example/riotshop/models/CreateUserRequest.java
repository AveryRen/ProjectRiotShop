package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class CreateUserRequest {
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("isAdmin")
    private Boolean isAdmin;
    
    @SerializedName("balance")
    private Double balance;
    
    public CreateUserRequest(String username, String email, String password, String fullName, 
                            String phoneNumber, String address, Boolean isAdmin, Double balance) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.isAdmin = isAdmin;
        this.balance = balance;
    }
    
    // Getters
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public Boolean getIsAdmin() { return isAdmin; }
    public Double getBalance() { return balance; }
}

