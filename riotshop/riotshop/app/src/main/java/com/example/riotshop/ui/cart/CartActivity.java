package com.example.riotshop.ui.cart;

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
import com.example.riotshop.models.CartItem;
import com.example.riotshop.ui.other.CheckoutActivity;
import com.example.riotshop.utils.SharedPrefManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnItemRemoveListener {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private TextView tvEmptyCart, tvTotalPrice;
    private Button btnCheckout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Ánh xạ Views
        rvCartItems = findViewById(R.id.rv_cart_items);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnCheckout = findViewById(R.id.btn_checkout);
        toolbar = findViewById(R.id.toolbar_cart);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        SharedPrefManager.getInstance(this);
        
        cartItems = new ArrayList<>();
        loadCart();

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            } else {
                // Calculate total amount
                double total = 0;
                for (CartItem item : cartItems) {
                    if (item.getProductTemplate() != null) {
                        total += item.getProductTemplate().getBasePrice() * item.getQuantity();
                    }
                }
                
                Intent intent = new Intent(this, CheckoutActivity.class);
                intent.putExtra("totalAmount", total);
                startActivity(intent);
            }
        });
    }

    private void loadCart() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            showEmptyCart();
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
                        if (cartItems.isEmpty()) {
                            showEmptyCart();
                        } else {
                            showCartItems();
                        }
                    } else {
                        showEmptyCart();
                    }
                } else {
                    showEmptyCart();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CartItem>>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi khi tải giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyCart();
            }
        });
    }

    private void showEmptyCart() {
        tvEmptyCart.setVisibility(View.VISIBLE);
        rvCartItems.setVisibility(View.GONE);
        findViewById(R.id.bottom_checkout_bar).setVisibility(View.GONE);
    }

    private void showCartItems() {
        tvEmptyCart.setVisibility(View.GONE);
        rvCartItems.setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_checkout_bar).setVisibility(View.VISIBLE);

        cartAdapter = new CartAdapter(this, cartItems);
        cartAdapter.setOnItemRemoveListener(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.getProductTemplate() != null) {
                total += item.getProductTemplate().getBasePrice() * item.getQuantity();
            }
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(String.format("Tổng cộng: %s", currencyFormat.format(total)));
    }

    @Override
    public void onItemRemove(CartItem cartItem) {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.removeCartItem("Bearer " + token, cartItem.getCartItemId());
        
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cartItems.remove(cartItem);
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                    
                    if (cartItems.isEmpty()) {
                        showEmptyCart();
                    }
                    
                    String productName = cartItem.getProductTemplate() != null ? 
                        cartItem.getProductTemplate().getTitle() : "Sản phẩm";
                    Toast.makeText(CartActivity.this, "Đã xóa '" + productName + "' khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CartActivity.this, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadCart(); // Reload cart when activity resumes
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
