package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class CreateOrderRequest {
    @SerializedName("accDetailId")
    private int accDetailId;
    
    @SerializedName("totalAmount")
    private double totalAmount;
    
    @SerializedName("paymentMethod")
    private String paymentMethod;

    public CreateOrderRequest(int accDetailId, double totalAmount, String paymentMethod) {
        this.accDetailId = accDetailId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }

    public int getAccDetailId() {
        return accDetailId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}

