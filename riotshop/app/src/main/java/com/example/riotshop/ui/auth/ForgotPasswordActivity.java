package com.example.riotshop.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.riotshop.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btnResetPassword = findViewById(R.id.btn_reset_password);

        // Xử lý nút Gửi yêu cầu (Frontend Mock)
        btnResetPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Yêu cầu đã được gửi! Quay lại Login...", Toast.LENGTH_LONG).show();
            // Sau khi backend xử lý thành công, quay lại Login
            finish();
        });
    }
}