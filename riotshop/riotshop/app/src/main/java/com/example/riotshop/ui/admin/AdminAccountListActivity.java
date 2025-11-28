package com.example.riotshop.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.adapters.AdminAccountAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAccountListActivity extends AppCompatActivity implements AdminAccountAdapter.OnAccountClickListener {

    private int templateId;
    private RecyclerView rvAccounts;
    private TextView tvEmptyAccounts, tvProductTitle, tvAccountStats;
    private AdminAccountAdapter accountAdapter;
    private List<Object> accountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_account_list);

        templateId = getIntent().getIntExtra("templateId", -1);
        if (templateId == -1) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        rvAccounts = findViewById(R.id.rv_accounts);
        tvEmptyAccounts = findViewById(R.id.tv_empty_accounts);
        tvProductTitle = findViewById(R.id.tv_product_title);
        tvAccountStats = findViewById(R.id.tv_account_stats);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý Tài khoản");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        accountList = new ArrayList<>();
        setupRecyclerView();
        loadProductInfo();
        loadAccounts();
    }

    private void setupRecyclerView() {
        accountAdapter = new AdminAccountAdapter(this, accountList);
        accountAdapter.setOnAccountClickListener(this);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        rvAccounts.setAdapter(accountAdapter);
    }

    @Override
    public void onAccountClick(Object account) {
        // Navigate to edit account
        Intent intent = new Intent(this, AdminEditAccountActivity.class);
        intent.putExtra("templateId", templateId);
        intent.putExtra("account", new Gson().toJson(account));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_accounts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_add_account) {
            Intent intent = new Intent(this, AdminAddAccountActivity.class);
            intent.putExtra("templateId", templateId);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_import_accounts) {
            Intent intent = new Intent(this, AdminImportAccountsActivity.class);
            intent.putExtra("templateId", templateId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProductInfo() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<com.example.riotshop.models.ProductTemplate>> call = apiService.getProductById(templateId);
        
        call.enqueue(new Callback<ApiResponse<com.example.riotshop.models.ProductTemplate>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.riotshop.models.ProductTemplate>> call, Response<ApiResponse<com.example.riotshop.models.ProductTemplate>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    tvProductTitle.setText(response.body().getData().getTitle());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.riotshop.models.ProductTemplate>> call, Throwable t) {
                // Ignore
            }
        });
    }

    private void loadAccounts() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Object>>> call = apiService.getProductAccounts("Bearer " + token, templateId);
        
        call.enqueue(new Callback<ApiResponse<List<Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Object>>> call, Response<ApiResponse<List<Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    accountList = response.body().getData();
                    accountAdapter.updateAccounts(accountList);
                    updateStats();
                    if (accountList.isEmpty()) {
                        showEmptyAccounts();
                    } else {
                        showAccounts();
                    }
                } else {
                    Toast.makeText(AdminAccountListActivity.this, "Lỗi khi tải tài khoản", Toast.LENGTH_SHORT).show();
                    showEmptyAccounts();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Object>>> call, Throwable t) {
                Toast.makeText(AdminAccountListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyAccounts();
            }
        });
    }

    private void updateStats() {
        int total = accountList.size();
        int available = 0;
        int sold = 0;
        
        Gson gson = new Gson();
        for (Object obj : accountList) {
            try {
                String json = gson.toJson(obj);
                Type type = new TypeToken<java.util.Map<String, Object>>(){}.getType();
                java.util.Map<String, Object> account = gson.fromJson(json, type);
                Boolean isSold = account.get("isSold") != null ? (Boolean) account.get("isSold") : false;
                if (isSold) {
                    sold++;
                } else {
                    available++;
                }
            } catch (Exception e) {
                // Skip
            }
        }
        
        tvAccountStats.setText(String.format("Tổng: %d | Còn lại: %d | Đã bán: %d", total, available, sold));
    }

    private void showEmptyAccounts() {
        tvEmptyAccounts.setVisibility(View.VISIBLE);
        rvAccounts.setVisibility(View.GONE);
    }

    private void showAccounts() {
        tvEmptyAccounts.setVisibility(View.GONE);
        rvAccounts.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAccounts();
    }
}

