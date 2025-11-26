package com.example.riotshop.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;

public class SignupActivity extends AppCompatActivity {

    private Button btnSignup;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Ánh xạ View
        btnSignup = findViewById(R.id.btn_signup);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        // Xử lý quay lại màn hình Đăng nhập (dùng finish())
        tvBackToLogin.setOnClickListener(v -> finish());

        // Xử lý nút Đăng ký (Frontend Mock)
        btnSignup.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng ký thành công! Quay lại Login...", Toast.LENGTH_LONG).show();
            // Sau khi backend xử lý thành công, quay lại Login
            finish();
        });
    }
}