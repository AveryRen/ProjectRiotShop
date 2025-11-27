package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class Order {
    @SerializedName("orderId")
    private int orderId;
    
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("totalAmount")
    private double totalAmount;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("orderDate")
    private String orderDate;
    
    @SerializedName("paymentMethod")
    private String paymentMethod;
    
    @SerializedName("accountDetail")
    private AccountDetail accountDetail;

    // Getters
    public int getOrderId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public AccountDetail getAccountDetail() {
        return accountDetail;
    }
}
