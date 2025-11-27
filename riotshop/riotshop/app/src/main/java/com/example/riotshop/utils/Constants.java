package com.example.riotshop.utils;

public class Constants {
    // API Base URL
    // QUAN TRỌNG: 
    // - Nếu dùng Android Emulator: dùng "http://10.0.2.2:5000/api/" (10.0.2.2 = localhost của máy host)
    // - Nếu dùng điện thoại thật trên cùng WiFi: dùng IP máy tính (hiện tại: 192.168.1.5)
    // - Đổi IP này theo máy bạn: chạy "ipconfig" (Windows) để xem IPv4 Address
    
    // Mặc định: Dùng cho Android Emulator
    // Backend C# ASP.NET Core chạy mặc định trên port 5000 (HTTP) hoặc 5001 (HTTPS)
    public static final String BASE_URL = "http://10.0.2.2:5000/api/";
    
    // Nếu dùng điện thoại thật, uncomment dòng dưới và comment dòng trên:
    // public static final String BASE_URL = "http://192.168.1.5:5000/api/";
    
    // API Endpoints
    public static final String ENDPOINT_AUTH = "auth/";
    public static final String ENDPOINT_USERS = "users/";
    public static final String ENDPOINT_PRODUCTS = "products/";
    public static final String ENDPOINT_ORDERS = "orders/";
    public static final String ENDPOINT_REVIEWS = "reviews/";
    public static final String ENDPOINT_WISHLIST = "wishlist/";
    
    // SharedPreferences Keys
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USERNAME = "username";
    public static final String PREF_EMAIL = "email";
    public static final String PREF_IS_ADMIN = "is_admin";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_TOKEN = "auth_token";
    
    // Request Timeout
    public static final int CONNECT_TIMEOUT = 30; // seconds
    public static final int READ_TIMEOUT = 30; // seconds
}