package com.example.riotshop.ui.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.UpdateUserAdminRequest;
import com.example.riotshop.models.UserResponse;
import com.example.riotshop.utils.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminEditUserActivity extends AppCompatActivity {

    private int userId;
    private UserResponse user;
    private EditText etEmail, etPassword, etFullName, etPhoneNumber, etAddress, etBalance;
    private CheckBox cbIsAdmin;
    private Button btnUpdate, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_user);

        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sửa người dùng");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        loadUser();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etFullName = findViewById(R.id.et_full_name);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etAddress = findViewById(R.id.et_address);
        etBalance = findViewById(R.id.et_balance);
        cbIsAdmin = findViewById(R.id.cb_is_admin);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
    }

    private void loadUser() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<UserResponse>>> call = apiService.getAdminUsers("Bearer " + token, null, null);

        call.enqueue(new Callback<ApiResponse<List<UserResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserResponse>>> call, Response<ApiResponse<List<UserResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<UserResponse> users = response.body().getData();
                    for (UserResponse u : users) {
                        if (u.getUserId() == userId) {
                            user = u;
                            break;
                        }
                    }
                    if (user != null) {
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Sửa người dùng: " + user.getUsername());
                        }
                        loadUserData();
                    } else {
                        Toast.makeText(AdminEditUserActivity.this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(AdminEditUserActivity.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserResponse>>> call, Throwable t) {
                Toast.makeText(AdminEditUserActivity.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadUserData() {
        if (user == null) return;
        
        // Load all user data to show what it was before
        if (user.getEmail() != null) {
            etEmail.setText(user.getEmail());
        }
        
        // Password field should remain empty (user can change it)
        etPassword.setText("");
        
        if (user.getFullName() != null) {
            etFullName.setText(user.getFullName());
        } else {
            etFullName.setText("");
        }
        
        if (user.getPhoneNumber() != null) {
            etPhoneNumber.setText(user.getPhoneNumber());
        } else {
            etPhoneNumber.setText("");
        }
        
        if (user.getAddress() != null) {
            etAddress.setText(user.getAddress());
        } else {
            etAddress.setText("");
        }
        
        etBalance.setText(String.valueOf(user.getBalance()));
        cbIsAdmin.setChecked(user.isAdmin());
    }

    private void setupListeners() {
        btnUpdate.setOnClickListener(v -> updateUser());
        btnDelete.setOnClickListener(v -> deleteUser());
    }

    private void updateUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        boolean isAdmin = cbIsAdmin.isChecked();
        double balance = 0;
        try {
            balance = Double.parseDouble(etBalance.getText().toString().trim());
        } catch (NumberFormatException e) {
            // Default to 0
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        UpdateUserAdminRequest request = new UpdateUserAdminRequest(
            email,
            password.isEmpty() ? null : password,
            fullName.isEmpty() ? null : fullName,
            phoneNumber.isEmpty() ? null : phoneNumber,
            address.isEmpty() ? null : address,
            isAdmin, balance
        );

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.updateUser("Bearer " + token, user.getUserId(), request);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminEditUserActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(AdminEditUserActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(AdminEditUserActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa người dùng " + user.getUsername() + "?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                String token = SharedPrefManager.getInstance(this).getToken();
                if (token == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                ApiService apiService = RetrofitClient.getInstance().getApiService();
                Call<ApiResponse<Object>> call = apiService.deleteUser("Bearer " + token, user.getUserId());

                call.enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AdminEditUserActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String error = response.body() != null ? response.body().getMessage() : response.message();
                            Toast.makeText(AdminEditUserActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(AdminEditUserActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

}

