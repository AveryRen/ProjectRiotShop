package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class PurchasedAccount {
    @SerializedName("orderId")
    private int orderId;
    
    @SerializedName("orderDate")
    private String orderDate;
    
    @SerializedName("totalAmount")
    private double totalAmount;
    
    @SerializedName("accountDetail")
    private AccountDetail accountDetail;

    // Getters
    public int getOrderId() {
        return orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public AccountDetail getAccountDetail() {
        return accountDetail;
    }
}

