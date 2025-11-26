package com.example.riotshop.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.ui.home.HomeActivity; // Activity đích sau khi Login

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ View
        btnLogin = findViewById(R.id.btn_login);
        tvSignUp = findViewById(R.id.tv_signup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        // Chuyển sang Đăng ký
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        // Chuyển sang Quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        // Xử lý Login (Frontend Mock)
        btnLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Đang đăng nhập... (Logic Backend sẽ xử lý)", Toast.LENGTH_SHORT).show();
            // CHUYỂN ĐẾN HOME SAU KHI XÁC THỰC THÀNH CÔNG
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        });
    }
}