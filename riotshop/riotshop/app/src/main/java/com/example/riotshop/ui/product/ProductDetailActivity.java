package com.example.riotshop.ui.product;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.AddToCartRequest;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.CartItem;
import com.example.riotshop.models.ProductTemplate;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvProductTitle, tvProductPrice, tvProductDescription;
    private Button btnBuyNow;
    private ImageButton btnAddToCart;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private int templateId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize Views from the new layout
        toolbar = findViewById(R.id.toolbar_product_detail);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        ivProductImage = findViewById(R.id.iv_product_detail_image);
        tvProductTitle = findViewById(R.id.tv_product_detail_title);
        tvProductPrice = findViewById(R.id.tv_product_detail_price);
        tvProductDescription = findViewById(R.id.tv_product_detail_description);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        btnAddToCart = findViewById(R.id.btn_detail_add_to_cart);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get templateId from intent
        templateId = getIntent().getIntExtra("templateId", 0);

        if (templateId > 0) {
            loadProductDetail();
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Click listeners
        btnBuyNow.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Mua ngay đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadProductDetail() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<ProductTemplate>> call = apiService.getProductById(templateId);

        call.enqueue(new Callback<ApiResponse<ProductTemplate>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductTemplate>> call, Response<ApiResponse<ProductTemplate>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ProductTemplate product = response.body().getData();
                    if (product != null) {
                        displayProduct(product);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductTemplate>> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi khi tải sản phẩm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProduct(ProductTemplate product) {
        collapsingToolbar.setTitle(product.getTitle());
        tvProductTitle.setText(product.getTitle());
        tvProductPrice.setText(FormatUtils.formatPrice(product.getBasePrice()));
        tvProductDescription.setText(product.getDescription() != null ? product.getDescription() : "Không có mô tả.");

        // Set a placeholder image. The logic to load a real image URL has been removed to fix compilation errors.
        ivProductImage.setImageResource(R.drawable.placeholder_account);
    }

    private void addToCart() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAddToCart.setEnabled(false);
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        AddToCartRequest request = new AddToCartRequest(templateId, 1);
        Call<ApiResponse<CartItem>> call = apiService.addToCart("Bearer " + token, request);

        call.enqueue(new Callback<ApiResponse<CartItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<CartItem>> call, Response<ApiResponse<CartItem>> response) {
                btnAddToCart.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CartItem>> call, Throwable t) {
                btnAddToCart.setEnabled(true);
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
