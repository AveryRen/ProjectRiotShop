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
import com.example.riotshop.models.UpdateAccountRequest;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminEditAccountActivity extends AppCompatActivity {

    private int templateId;
    private int accDetailId;
    private EditText etUsername, etPassword, etRiotId, etRecoveryEmail, etOriginalPrice;
    private CheckBox cbIsDropMail, cbIsSold;
    private Button btnUpdate, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_account);

        templateId = getIntent().getIntExtra("templateId", -1);
        String accountJson = getIntent().getStringExtra("account");
        
        if (templateId == -1 || accountJson == null) {
            Toast.makeText(this, "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sửa tài khoản");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        loadAccountData(accountJson);
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_account_username);
        etPassword = findViewById(R.id.et_account_password);
        etRiotId = findViewById(R.id.et_riot_id);
        etRecoveryEmail = findViewById(R.id.et_recovery_email);
        etOriginalPrice = findViewById(R.id.et_original_price);
        cbIsDropMail = findViewById(R.id.cb_is_drop_mail);
        cbIsSold = findViewById(R.id.cb_is_sold);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
    }

    private void loadAccountData(String accountJson) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> account = gson.fromJson(accountJson, type);

            accDetailId = account.get("accDetailId") != null ? 
                ((Double) account.get("accDetailId")).intValue() : 0;
            String username = account.get("accountUsername") != null ? 
                (String) account.get("accountUsername") : "";
            String riotId = account.get("riotId") != null ? 
                (String) account.get("riotId") : "";
            String recoveryEmail = account.get("recoveryEmail") != null ? 
                (String) account.get("recoveryEmail") : "";
            Double originalPrice = account.get("originalPrice") != null ? 
                (Double) account.get("originalPrice") : 0.0;
            Boolean isDropMail = account.get("isDropMail") != null ? 
                (Boolean) account.get("isDropMail") : false;
            Boolean isSold = account.get("isSold") != null ? 
                (Boolean) account.get("isSold") : false;

            etUsername.setText(username);
            etRiotId.setText(riotId);
            etRecoveryEmail.setText(recoveryEmail);
            etOriginalPrice.setText(String.valueOf(originalPrice));
            cbIsDropMail.setChecked(isDropMail);
            cbIsSold.setChecked(isSold);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi đọc dữ liệu tài khoản", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        btnUpdate.setOnClickListener(v -> updateAccount());
        btnDelete.setOnClickListener(v -> deleteAccount());
    }

    private void updateAccount() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String riotId = etRiotId.getText().toString().trim();
        String recoveryEmail = etRecoveryEmail.getText().toString().trim();
        boolean isDropMail = cbIsDropMail.isChecked();
        boolean isSold = cbIsSold.isChecked();
        double originalPrice = 0;
        
        try {
            String priceStr = etOriginalPrice.getText().toString().trim();
            if (!priceStr.isEmpty()) {
                originalPrice = Double.parseDouble(priceStr);
            }
        } catch (NumberFormatException e) {
            // Default to 0
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Tên đăng nhập không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        UpdateAccountRequest request = new UpdateAccountRequest(
            username,
            password.isEmpty() ? null : password,
            riotId.isEmpty() ? null : riotId,
            recoveryEmail.isEmpty() ? null : recoveryEmail,
            isDropMail,
            originalPrice > 0 ? originalPrice : null,
            isSold
        );

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.updateAccount("Bearer " + token, accDetailId, request);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminEditAccountActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(AdminEditAccountActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(AdminEditAccountActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAccount() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa tài khoản này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                String token = SharedPrefManager.getInstance(this).getToken();
                if (token == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                ApiService apiService = RetrofitClient.getInstance().getApiService();
                Call<ApiResponse<Object>> call = apiService.deleteAccount("Bearer " + token, accDetailId);

                call.enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AdminEditAccountActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String error = response.body() != null ? response.body().getMessage() : response.message();
                            Toast.makeText(AdminEditAccountActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(AdminEditAccountActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}

