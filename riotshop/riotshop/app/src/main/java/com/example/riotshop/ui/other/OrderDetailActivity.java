package com.example.riotshop.ui.other;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.AccountDetail;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.Order;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvOrderId, tvStatus, tvTotalAmount, tvOrderDate, tvPaymentMethod;
    private TextView tvProductName, tvGameName, tvAccountUsername, tvAccountPassword, tvAccountRiotId;
    private LinearLayout llAccountInfo;
    private Button btnCancelOrder, btnCopyUsername, btnCopyPassword, btnCopyRiotId;
    private int orderId;
    private String actualPassword = ""; // Store actual password for copying

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        SharedPrefManager.getInstance(this);

        orderId = getIntent().getIntExtra("orderId", 0);
        if (orderId == 0) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar_order_detail);
        tvOrderId = findViewById(R.id.tv_order_id);
        tvStatus = findViewById(R.id.tv_order_status);
        tvTotalAmount = findViewById(R.id.tv_order_total);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        
        // Account info views
        llAccountInfo = findViewById(R.id.ll_account_info);
        tvProductName = findViewById(R.id.tv_product_name);
        tvGameName = findViewById(R.id.tv_game_name);
        tvAccountUsername = findViewById(R.id.tv_account_username);
        tvAccountPassword = findViewById(R.id.tv_account_password);
        tvAccountRiotId = findViewById(R.id.tv_account_riot_id);
        btnCopyUsername = findViewById(R.id.btn_copy_username);
        btnCopyPassword = findViewById(R.id.btn_copy_password);
        btnCopyRiotId = findViewById(R.id.btn_copy_riot_id);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chi tiết đơn hàng");

        btnCancelOrder.setOnClickListener(v -> showCancelDialog());
        
        // Copy button listeners
        btnCopyUsername.setOnClickListener(v -> {
            String username = tvAccountUsername.getText().toString();
            if (!username.isEmpty() && !username.equals("••••••••")) {
                copyToClipboard("Tên đăng nhập", username);
            }
        });
        btnCopyPassword.setOnClickListener(v -> {
            if (!actualPassword.isEmpty()) {
                copyToClipboard("Mật khẩu", actualPassword);
            }
        });
        btnCopyRiotId.setOnClickListener(v -> {
            String riotId = tvAccountRiotId.getText().toString();
            if (!riotId.isEmpty()) {
                copyToClipboard("Riot ID", riotId);
            }
        });

        loadOrderDetail();
    }

    private void loadOrderDetail() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.getOrderById("Bearer " + token, orderId);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        // Parse the response to get order details with account info
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.toJsonTree(apiResponse.getData()).getAsJsonObject();
                        Order order = gson.fromJson(jsonObject, Order.class);
                        
                        // Parse account detail if exists
                        if (jsonObject.has("accountDetail") && !jsonObject.get("accountDetail").isJsonNull()) {
                            JsonObject accountDetailJson = jsonObject.getAsJsonObject("accountDetail");
                            AccountDetail accountDetail = gson.fromJson(accountDetailJson, AccountDetail.class);
                            order = gson.fromJson(jsonObject, Order.class);
                            
                            // Manually set account detail using reflection or create a setter
                            // For now, we'll parse it separately
                            displayOrder(order, accountDetail, jsonObject);
                        } else {
                            displayOrder(order, null, jsonObject);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi khi tải đơn hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrder(Order order, AccountDetail accountDetail, JsonObject jsonObject) {
        tvOrderId.setText("Đơn hàng #" + order.getOrderId());
        tvStatus.setText(order.getStatus());
        tvTotalAmount.setText(FormatUtils.formatPrice(order.getTotalAmount()));
        tvOrderDate.setText(order.getOrderDate());
        tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");

        // Show cancel button only if order can be cancelled
        if ("Pending".equals(order.getStatus()) || "Processing".equals(order.getStatus())) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }
        
        // Display account information if order is completed and has account detail
        if ("completed".equalsIgnoreCase(order.getStatus()) && jsonObject.has("accountDetail") && !jsonObject.get("accountDetail").isJsonNull()) {
            JsonObject accountDetailJson = jsonObject.getAsJsonObject("accountDetail");
            
            // Get product template info
            if (accountDetailJson.has("productTemplate") && !accountDetailJson.get("productTemplate").isJsonNull()) {
                JsonObject productTemplateJson = accountDetailJson.getAsJsonObject("productTemplate");
                if (productTemplateJson.has("title")) {
                    tvProductName.setText(productTemplateJson.get("title").getAsString());
                }
                if (productTemplateJson.has("gameName")) {
                    tvGameName.setText(productTemplateJson.get("gameName").getAsString());
                }
            }
            
            // Get account credentials
            if (accountDetailJson.has("accountUsername")) {
                tvAccountUsername.setText(accountDetailJson.get("accountUsername").getAsString());
            }
            if (accountDetailJson.has("accountPassword")) {
                actualPassword = accountDetailJson.get("accountPassword").getAsString();
                // Mask password for display
                tvAccountPassword.setText(actualPassword != null && !actualPassword.isEmpty() ? "••••••••" : "");
            }
            if (accountDetailJson.has("riotId")) {
                tvAccountRiotId.setText(accountDetailJson.get("riotId").getAsString());
            }
            
            llAccountInfo.setVisibility(View.VISIBLE);
        } else {
            llAccountInfo.setVisibility(View.GONE);
        }
    }
    
    private void copyToClipboard(String label, String text) {
        if (text == null || text.isEmpty() || text.equals("••••••••")) {
            Toast.makeText(this, "Không thể sao chép", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Đã sao chép " + label, Toast.LENGTH_SHORT).show();
    }

    private void showCancelDialog() {
        CancelOrderDialog dialog = new CancelOrderDialog(orderId);
        dialog.show(getSupportFragmentManager(), "CancelOrderDialog");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
