package com.example.riotshop.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.riotshop.models.User; // Giả sử Model User.java đã tồn tại

public class SharedPrefManager {

    private static final String SHARED_PREF_NAME = "RiotShopSharedPref";
    private static final String KEY_USER_ID = "keyUserId";
    private static final String KEY_USERNAME = "keyUsername";
    private static final String KEY_USER_ROLE = "keyUserRole";
    private static final String KEY_IS_LOGGED_IN = "keyIsLoggedIn";
    private static final String KEY_TOKEN = "keyToken";
    private static final String KEY_IS_ADMIN = "keyIsAdmin";

    private static SharedPrefManager instance;
    private static Context ctx;

    // Constructor Private
    private SharedPrefManager(Context context) {
        ctx = context;
    }

    // Phương thức STATIC và SYNCHRONIZED để lấy thể hiện (instance) duy nhất
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }
    
    // Get instance without context (for use in interceptors)
    public static synchronized SharedPrefManager getInstance() {
        return instance;
    }

    // Phương thức giả định dùng khi LoginActivity lưu thông tin
    public void saveUserLogin(User user) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Cần thiết để HomeActivity hiển thị tên
        editor.putString(KEY_USERNAME, user.getUsername());

        editor.apply();
    }

    // Lấy tên người dùng (được gọi trong HomeActivity)
    public String getUsername() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        // Trả về "Khách" nếu chưa tìm thấy
        return sharedPreferences.getString(KEY_USERNAME, "Khách");
    }

    // Đăng xuất (được gọi trong HomeActivity)
    public void logout() {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    
    // Save token
    public void saveToken(String token) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }
    
    // Get token
    public String getToken() {
        if (ctx == null) return null;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
    
    // Save isAdmin status
    public void saveIsAdmin(boolean isAdmin) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.apply();
    }
    
    // Get isAdmin status
    public boolean isAdmin() {
        if (ctx == null) return false;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_ADMIN, false);
    }
    
    // Save userId
    public void saveUserId(int userId) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }
    
    // Get userId
    public int getUserId() {
        if (ctx == null) return 0;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER_ID, 0);
    }

    // ... (Các phương thức khác)
}