package com.example.riotshop.ui.admin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.BulkCreateAccountsRequest;
import com.example.riotshop.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminImportAccountsActivity extends AppCompatActivity {

    private int templateId;
    private EditText etAccountsText;
    private TextView tvFormatHint;
    private Button btnImport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_import_accounts);

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
            getSupportActionBar().setTitle("Import nhiều tài khoản");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etAccountsText = findViewById(R.id.et_accounts_text);
        tvFormatHint = findViewById(R.id.tv_format_hint);
        btnImport = findViewById(R.id.btn_import);
        
        tvFormatHint.setText("Format: mỗi dòng một tài khoản\n" +
            "username:password:riotId:email\n" +
            "hoặc\n" +
            "username:password");
    }

    private void setupListeners() {
        btnImport.setOnClickListener(v -> importAccounts());
    }

    private void importAccounts() {
        String text = etAccountsText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập danh sách tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] lines = text.split("\n");
        List<BulkCreateAccountsRequest.AccountInput> accounts = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(":");
            if (parts.length < 2) {
                continue; // Skip invalid lines
            }

            String username = parts[0].trim();
            String password = parts[1].trim();
            String riotId = parts.length > 2 ? parts[2].trim() : null;
            String email = parts.length > 3 ? parts[3].trim() : null;

            if (!username.isEmpty() && !password.isEmpty()) {
                accounts.add(new BulkCreateAccountsRequest.AccountInput(
                    username, password, 
                    (riotId != null && !riotId.isEmpty()) ? riotId : null,
                    (email != null && !email.isEmpty()) ? email : null,
                    false, null
                ));
            }
        }

        if (accounts.isEmpty()) {
            Toast.makeText(this, "Không có tài khoản hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BulkCreateAccountsRequest request = new BulkCreateAccountsRequest(accounts);

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.bulkCreateAccounts("Bearer " + token, templateId, request);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Parse response to get created count
                    Toast.makeText(AdminImportAccountsActivity.this, 
                        "Import thành công " + accounts.size() + " tài khoản", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(AdminImportAccountsActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(AdminImportAccountsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

