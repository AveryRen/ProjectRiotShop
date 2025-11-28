package com.example.riotshop.ui.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.CreateAccountRequest;
import com.example.riotshop.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddAccountActivity extends AppCompatActivity {

    private int templateId;
    private EditText etUsername, etPassword, etRiotId, etRecoveryEmail, etOriginalPrice;
    private CheckBox cbIsDropMail;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_account);

        templateId = getIntent().getIntExtra("templateId", -1);
        if (templateId == -1) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm tài khoản");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_account_username);
        etPassword = findViewById(R.id.et_account_password);
        etRiotId = findViewById(R.id.et_riot_id);
        etRecoveryEmail = findViewById(R.id.et_recovery_email);
        etOriginalPrice = findViewById(R.id.et_original_price);
        cbIsDropMail = findViewById(R.id.cb_is_drop_mail);
        btnCreate = findViewById(R.id.btn_create);
    }

    private void setupListeners() {
        btnCreate.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String riotId = etRiotId.getText().toString().trim();
        String recoveryEmail = etRecoveryEmail.getText().toString().trim();
        boolean isDropMail = cbIsDropMail.isChecked();
        double originalPrice = 0;
        
        try {
            String priceStr = etOriginalPrice.getText().toString().trim();
            if (!priceStr.isEmpty()) {
                originalPrice = Double.parseDouble(priceStr);
            }
        } catch (NumberFormatException e) {
            // Default to 0
        }

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Tên đăng nhập và mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        CreateAccountRequest request = new CreateAccountRequest(
            username, password,
            riotId.isEmpty() ? null : riotId,
            recoveryEmail.isEmpty() ? null : recoveryEmail,
            isDropMail,
            originalPrice > 0 ? originalPrice : null
        );

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.createAccount("Bearer " + token, templateId, request);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminAddAccountActivity.this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(AdminAddAccountActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(AdminAddAccountActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

