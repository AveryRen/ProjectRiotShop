package com.example.riotshop.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.AuthResponse;
import com.example.riotshop.models.LoginRequest;
import com.example.riotshop.models.UserResponse;
import com.example.riotshop.ui.admin.AdminDashboardActivity;
import com.example.riotshop.ui.home.HomeActivity;
import com.example.riotshop.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private EditText etEmail, etPassword;
    private TextView tvSignUp, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SharedPrefManager
        SharedPrefManager.getInstance(this);

        // Ánh xạ View
        btnLogin = findViewById(R.id.btn_login);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
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

        // Xử lý Login với API thực
        btnLogin.setOnClickListener(v -> {
            String username = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(username, password);
        });
    }

    private void performLogin(String username, String password) {
        btnLogin.setEnabled(false);
        Toast.makeText(this, "Đang đăng nhập...", Toast.LENGTH_SHORT).show();

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<ApiResponse<AuthResponse>> call = apiService.login(loginRequest);

        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                btnLogin.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse authResponse = apiResponse.getData();
                        
                        // Save token
                        SharedPrefManager.getInstance(LoginActivity.this).saveToken(authResponse.getToken());
                        
                        // Save user info
                        if (authResponse.getUser() != null) {
                            UserResponse user = authResponse.getUser();
                            SharedPrefManager.getInstance(LoginActivity.this).saveUserLogin(
                                    new com.example.riotshop.models.User(
                                            user.getUsername(),
                                            user.getEmail()
                                    )
                            );
                            // Save isAdmin status
                            SharedPrefManager.getInstance(LoginActivity.this).saveIsAdmin(user.isAdmin());
                            // Save userId
                            SharedPrefManager.getInstance(LoginActivity.this).saveUserId(user.getUserId());
                        }
                        
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Check if user is admin and redirect accordingly
                        if (authResponse.getUser() != null && authResponse.getUser().isAdmin()) {
                            // Redirect to Admin Dashboard
                            startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                        } else {
                            // Redirect to Home Activity for regular users
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, 
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đăng nhập thất bại", 
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}