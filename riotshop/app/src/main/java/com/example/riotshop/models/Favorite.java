package com.example.riotshop.models;

public class Favorite {
    private String accountId;
    private String userId;
    private long timestamp;

    public Favorite() {
        // Constructor trống cho Firebase
    }

    public Favorite(String accountId, String userId, long timestamp) {
        this.accountId = accountId;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters
    public String getAccountId() { return accountId; }
    public String getUserId() { return userId; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
