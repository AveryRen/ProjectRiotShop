package com.example.riotshop.models;

public class User {
    private String uid;
    private String username; // Phải có thuộc tính này
    private String email;
    // ... các thuộc tính khác ...

    public User() {
    }

    // Constructor đầy đủ
    public User(String uid, String username, String email, String profileImage, String role, long balance) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        // ...
    }

    // 🔑 PHƯƠNG THỨC CẦN THIẾT ĐỂ KHẮC PHỤC LỖI Cannot resolve method 'getUsername'
    public String getUsername() {
        return username;
    }

    // ... Các Getters và Setters khác ...
}