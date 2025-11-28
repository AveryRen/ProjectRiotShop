package com.example.riotshop.ui.admin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class AdminOrderDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvOrderId, tvStatus, tvTotalAmount, tvOrderDate, tvPaymentMethod;
    private TextView tvUsername, tvEmail, tvProductName, tvGameName, tvAccountUsername, tvAccountPassword, tvAccountRiotId;
    private LinearLayout llAccountInfo, llUserInfo;
    private Button btnDeleteOrder, btnCopyUsername, btnCopyPassword, btnCopyRiotId;
    private int orderId;
    private String actualPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        SharedPrefManager.getInstance(this);

        orderId = getIntent().getIntExtra("orderId", 0);
        if (orderId == 0) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        tvOrderId = findViewById(R.id.tv_order_id);
        tvStatus = findViewById(R.id.tv_order_status);
        tvTotalAmount = findViewById(R.id.tv_order_total);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        btnDeleteOrder = findViewById(R.id.btn_delete_order);
        
        llUserInfo = findViewById(R.id.ll_user_info);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đơn hàng");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        btnDeleteOrder.setOnClickListener(v -> deleteOrder());
        
        btnCopyUsername.setOnClickListener(v -> {
            String username = tvAccountUsername.getText().toString();
            if (!username.isEmpty()) {
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
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.getAdminOrderById("Bearer " + token, orderId);
        
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object orderObj = response.body().getData();
                    displayOrderDetail(orderObj);
                } else {
                    Toast.makeText(AdminOrderDetailActivity.this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(AdminOrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrderDetail(Object orderObj) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(orderObj);
            JsonObject orderJson = gson.fromJson(json, JsonObject.class);

            // Order info
            if (orderJson.has("orderId")) {
                tvOrderId.setText("ID: " + orderJson.get("orderId").getAsInt());
            }
            if (orderJson.has("status")) {
                tvStatus.setText("Trạng thái: " + orderJson.get("status").getAsString());
            }
            if (orderJson.has("totalAmount")) {
                tvTotalAmount.setText("Tổng tiền: " + FormatUtils.formatPrice(orderJson.get("totalAmount").getAsDouble()));
            }
            if (orderJson.has("orderDate")) {
                tvOrderDate.setText("Ngày đặt: " + orderJson.get("orderDate").getAsString());
            }
            if (orderJson.has("paymentMethod")) {
                tvPaymentMethod.setText("Phương thức: " + orderJson.get("paymentMethod").getAsString());
            }

            // User info
            if (orderJson.has("user")) {
                JsonObject user = orderJson.getAsJsonObject("user");
                if (user.has("username")) {
                    tvUsername.setText("Người dùng: " + user.get("username").getAsString());
                }
                if (user.has("email")) {
                    tvEmail.setText("Email: " + user.get("email").getAsString());
                }
                llUserInfo.setVisibility(View.VISIBLE);
            }

            // Account info
            if (orderJson.has("accountDetail")) {
                JsonObject accountDetail = orderJson.getAsJsonObject("accountDetail");
                if (accountDetail.has("accountUsername")) {
                    tvAccountUsername.setText(accountDetail.get("accountUsername").getAsString());
                    actualPassword = accountDetail.has("accountPassword") ? 
                        accountDetail.get("accountPassword").getAsString() : "";
                    tvAccountPassword.setText("••••••••");
                }
                if (accountDetail.has("riotId")) {
                    tvAccountRiotId.setText(accountDetail.get("riotId").getAsString());
                }
                if (accountDetail.has("productTemplate")) {
                    JsonObject product = accountDetail.getAsJsonObject("productTemplate");
                    if (product.has("title")) {
                        tvProductName.setText(product.get("title").getAsString());
                    }
                    if (product.has("gameName")) {
                        tvGameName.setText(product.get("gameName").getAsString());
                    }
                }
                llAccountInfo.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi hiển thị đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteOrder() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa đơn hàng này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                String token = SharedPrefManager.getInstance(this).getToken();
                if (token == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                ApiService apiService = RetrofitClient.getInstance().getApiService();
                Call<ApiResponse<Object>> call = apiService.deleteOrder("Bearer " + token, orderId);

                call.enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AdminOrderDetailActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String error = response.body() != null ? response.body().getMessage() : response.message();
                            Toast.makeText(AdminOrderDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(AdminOrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Đã copy " + label, Toast.LENGTH_SHORT).show();
    }
}
