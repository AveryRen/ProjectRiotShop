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

    // ... (Các phương thức khác)
}