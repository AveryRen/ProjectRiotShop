package com.example.riotshop.models;

import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private String userName;
    private long orderDate;
    private double totalPrice;
    private String status; // Ví dụ: Pending, Completed, Cancelled
    private List<CartItem> items;
    private String address;
    private String phoneNumber;

    public Order() {
        // Constructor trống cho Firebase
    }

    public Order(String orderId, String userId, String userName, long orderDate, double totalPrice, String status, List<CartItem> items, String address, String phoneNumber) {
        this.orderId = orderId;
        this.userId = userId;
        this.userName = userName;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.items = items;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public long getOrderDate() { return orderDate; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public List<CartItem> getItems() { return items; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }

    // Setters
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setOrderDate(long orderDate) { this.orderDate = orderDate; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(String status) { this.status = status; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}