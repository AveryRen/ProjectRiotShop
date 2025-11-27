package com.example.riotshop.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminStatisticsActivity extends AppCompatActivity {

    private TextView tvTotalUsers, tvTotalOrders, tvTotalRevenue, tvTotalProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_statistics);

        SharedPrefManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvTotalProducts = findViewById(R.id.tv_total_products);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thống Kê");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        loadStatistics();
    }

    private void loadStatistics() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.getAdminStatistics("Bearer " + token);
        
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data != null) {
                        try {
                            Gson gson = new Gson();
                            String json = gson.toJson(data);
                            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                            
                            int totalUsers = jsonObject.has("totalUsers") ? jsonObject.get("totalUsers").getAsInt() : 0;
                            int totalOrders = jsonObject.has("totalOrders") ? jsonObject.get("totalOrders").getAsInt() : 0;
                            double totalRevenue = jsonObject.has("totalRevenue") ? jsonObject.get("totalRevenue").getAsDouble() : 0;
                            int totalProducts = jsonObject.has("totalProducts") ? jsonObject.get("totalProducts").getAsInt() : 0;
                            
                            tvTotalUsers.setText(String.valueOf(totalUsers));
                            tvTotalOrders.setText(String.valueOf(totalOrders));
                            tvTotalRevenue.setText(FormatUtils.formatPrice(totalRevenue));
                            tvTotalProducts.setText(String.valueOf(totalProducts));
                        } catch (Exception e) {
                            Toast.makeText(AdminStatisticsActivity.this, "Lỗi khi parse dữ liệu", Toast.LENGTH_SHORT).show();
                            setDefaultValues();
                        }
                    } else {
                        setDefaultValues();
                    }
                } else {
                    Toast.makeText(AdminStatisticsActivity.this, "Lỗi khi tải thống kê: " + response.message(), Toast.LENGTH_SHORT).show();
                    setDefaultValues();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(AdminStatisticsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                setDefaultValues();
            }
        });
    }

    private void setDefaultValues() {
        tvTotalUsers.setText("0");
        tvTotalOrders.setText("0");
        tvTotalRevenue.setText("0 VNĐ");
        tvTotalProducts.setText("0");
    }
}
