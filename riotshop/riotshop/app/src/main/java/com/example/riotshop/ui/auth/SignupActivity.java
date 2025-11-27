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
import com.example.riotshop.models.RegisterRequest;
import com.example.riotshop.models.UserResponse;
import com.example.riotshop.ui.admin.AdminDashboardActivity;
import com.example.riotshop.ui.home.HomeActivity;
import com.example.riotshop.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private Button btnSignup;
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize SharedPrefManager
        SharedPrefManager.getInstance(this);

        // Ánh xạ View
        btnSignup = findViewById(R.id.btn_signup);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email_signup);
        etPassword = findViewById(R.id.et_password_signup);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        // Xử lý quay lại màn hình Đăng nhập
        tvBackToLogin.setOnClickListener(v -> finish());

        // Xử lý nút Đăng ký với API thực
        btnSignup.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            performRegister(username, email, password);
        });
    }

    private void performRegister(String username, String email, String password) {
        btnSignup.setEnabled(false);
        Toast.makeText(this, "Đang đăng ký...", Toast.LENGTH_SHORT).show();

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        RegisterRequest registerRequest = new RegisterRequest(username, password, email, null, null);
        Call<ApiResponse<AuthResponse>> call = apiService.register(registerRequest);

        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                btnSignup.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse authResponse = apiResponse.getData();
                        
                        // Save token
                        SharedPrefManager.getInstance(SignupActivity.this).saveToken(authResponse.getToken());
                        
                        // Save user info
                        if (authResponse.getUser() != null) {
                            UserResponse user = authResponse.getUser();
                            SharedPrefManager.getInstance(SignupActivity.this).saveUserLogin(
                                    new com.example.riotshop.models.User(
                                            user.getUsername(),
                                            user.getEmail()
                                    )
                            );
                            // Save isAdmin status
                            SharedPrefManager.getInstance(SignupActivity.this).saveIsAdmin(user.isAdmin());
                        }
                        
                        Toast.makeText(SignupActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Redirect based on user role
                        if (authResponse.getUser() != null && authResponse.getUser().isAdmin()) {
                            startActivity(new Intent(SignupActivity.this, AdminDashboardActivity.class));
                        } else {
                            startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(SignupActivity.this, 
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đăng ký thất bại", 
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                btnSignup.setEnabled(true);
                Toast.makeText(SignupActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}