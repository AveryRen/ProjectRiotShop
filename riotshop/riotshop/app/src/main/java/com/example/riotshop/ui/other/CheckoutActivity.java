package com.example.riotshop.ui.other;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.adapters.CartAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.AvailableAccount;
import com.example.riotshop.models.CartItem;
import com.example.riotshop.models.CreateOrderRequest;
import com.example.riotshop.models.Order;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTotalAmount;
    private Button btnPlaceOrder;
    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private double totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        SharedPrefManager.getInstance(this);

        toolbar = findViewById(R.id.toolbar_checkout);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        rvCartItems = findViewById(R.id.rv_checkout_cart_items);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thanh toán");

        // Get total amount from intent
        totalAmount = getIntent().getDoubleExtra("totalAmount", 0);
        
        cartItems = new ArrayList<>();
        loadCart();
        
        tvTotalAmount.setText(String.format("Tổng tiền: %s", FormatUtils.formatPrice(totalAmount)));

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }
    
    private void loadCart() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<CartItem>>> call = apiService.getCart("Bearer " + token);
        
        call.enqueue(new Callback<ApiResponse<List<CartItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CartItem>>> call, Response<ApiResponse<List<CartItem>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<CartItem>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        cartItems.clear();
                        cartItems.addAll(apiResponse.getData());
                        
                        // Recalculate total if needed
                        if (totalAmount == 0) {
                            totalAmount = 0;
                            for (CartItem item : cartItems) {
                                if (item.getProductTemplate() != null) {
                                    totalAmount += item.getProductTemplate().getBasePrice() * item.getQuantity();
                                }
                            }
                            tvTotalAmount.setText(String.format("Tổng tiền: %s", FormatUtils.formatPrice(totalAmount)));
                        }
                        
                        setupCartItemsRecyclerView();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CartItem>>> call, Throwable t) {
                Toast.makeText(CheckoutActivity.this, "Lỗi khi tải giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupCartItemsRecyclerView() {
        cartAdapter = new CartAdapter(this, cartItems);
        // Disable remove functionality in checkout
        cartAdapter.setOnItemRemoveListener(null);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void placeOrder() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPlaceOrder.setEnabled(false);
        Toast.makeText(this, "Đang xử lý đơn hàng...", Toast.LENGTH_SHORT).show();

        // Lấy available account cho item đầu tiên trong giỏ hàng
        CartItem firstItem = cartItems.get(0);
        if (firstItem.getProductTemplate() == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            btnPlaceOrder.setEnabled(true);
            return;
        }

        int templateId = firstItem.getProductTemplate().getTemplateId();
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        
        // Bước 1: Lấy available account detail ID
        Call<ApiResponse<Object>> getAccountCall = apiService.getAvailableAccount(templateId);
        getAccountCall.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Parse available account
                    Gson gson = new Gson();
                    AvailableAccount availableAccount = gson.fromJson(
                        gson.toJson(response.body().getData()), 
                        AvailableAccount.class
                    );
                    
                    if (availableAccount != null && availableAccount.getAccDetailId() > 0) {
                        // Bước 2: Tạo order với account detail ID
                        createOrderWithAccount(token, availableAccount.getAccDetailId(), totalAmount);
                    } else {
                        btnPlaceOrder.setEnabled(true);
                        Toast.makeText(CheckoutActivity.this, "Không có tài khoản khả dụng cho sản phẩm này", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    btnPlaceOrder.setEnabled(true);
                    String errorMsg = "Không có tài khoản khả dụng";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(CheckoutActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, "Lỗi khi lấy tài khoản: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void createOrderWithAccount(String token, int accDetailId, double totalAmount) {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        CreateOrderRequest request = new CreateOrderRequest(accDetailId, totalAmount, "Balance");
        Call<ApiResponse<Order>> call = apiService.createOrder("Bearer " + token, request);

        call.enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                btnPlaceOrder.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate đến trang tài khoản đã mua
                    Intent intent = new Intent(CheckoutActivity.this, PurchasedAccountsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Lỗi khi đặt hàng";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(CheckoutActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
