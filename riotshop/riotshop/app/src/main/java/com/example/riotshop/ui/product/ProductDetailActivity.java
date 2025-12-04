package com.example.riotshop.ui.product;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.adapters.ProductAdapter;
import com.example.riotshop.adapters.ReviewAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.AddToCartRequest;
import com.example.riotshop.models.AddWishlistRequest;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.CartItem;
import com.example.riotshop.models.Product;
import com.example.riotshop.models.ProductTemplate;
import com.example.riotshop.models.Review;
import com.example.riotshop.models.Wishlist;

import java.util.List;
import com.example.riotshop.ui.comment.AddCommentActivity;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener, ProductAdapter.OnFavoriteClickListener {

    private ImageView ivProductImage;
    private TextView tvProductTitle, tvProductPrice, tvProductDescription, tvProductQuantity;
    private Button btnBuyNow;
    private ImageButton btnAddToCart;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private int templateId;
    private RecyclerView rvRelatedProducts, rvReviews;
    private ProductAdapter relatedProductAdapter;
    private ReviewAdapter reviewAdapter;
    private List<Product> relatedProducts;
    private List<Review> reviews;
    private ProductTemplate currentProduct;

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
        // tvProductQuantity may not exist in layout - will be null
        tvProductQuantity = null;
        btnBuyNow = findViewById(R.id.btn_buy_now);
        btnAddToCart = findViewById(R.id.btn_detail_add_to_cart);
        
        // RecyclerViews don't exist in current layout - set to null
        rvRelatedProducts = null;
        rvReviews = null;
        
        // Initialize lists (even if RecyclerViews don't exist)
        relatedProducts = new java.util.ArrayList<Product>();
        reviews = new java.util.ArrayList<Review>();

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
            // Sử dụng templateId từ intent thay vì currentProduct để tránh bug
            if (templateId > 0) {
                buyNow(templateId);
            } else {
                Toast.makeText(this, "Đang tải thông tin sản phẩm...", Toast.LENGTH_SHORT).show();
            }
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
                        currentProduct = product; // Save product for later use
                        displayProduct(product);
                    } else {
                        android.util.Log.e("ProductDetail", "Product data is null");
                        Toast.makeText(ProductDetailActivity.this, "Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    android.util.Log.e("ProductDetail", "API response not successful: " + (response.body() != null ? response.body().getMessage() : response.message()));
                    Toast.makeText(ProductDetailActivity.this, "Lỗi tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductTemplate>> call, Throwable t) {
                android.util.Log.e("ProductDetail", "Failed to load product: " + t.getMessage(), t);
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayProduct(ProductTemplate product) {
        android.util.Log.d("ProductDetail", "=== displayProduct called ===");
        android.util.Log.d("ProductDetail", "Product: " + product.getTitle());
        android.util.Log.d("ProductDetail", "ImageView is null: " + (ivProductImage == null));
        
        collapsingToolbar.setTitle(product.getTitle());
        tvProductTitle.setText(product.getTitle());
        tvProductPrice.setText(FormatUtils.formatPrice(product.getBasePrice()));
        tvProductDescription.setText(product.getDescription() != null ? product.getDescription() : "");
        
        // Load image from URL if available, otherwise use placeholder
        String imageUrl = product.getImageUrl();
        android.util.Log.d("ProductDetail", "Product imageUrl: " + imageUrl);
        
        if (ivProductImage == null) {
            android.util.Log.e("ProductDetail", "ImageView is null! Cannot load image.");
            return;
        }
        
        // Ensure ImageView is visible
        ivProductImage.setVisibility(View.VISIBLE);
        android.util.Log.d("ProductDetail", "ImageView visibility set to VISIBLE");
        
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.trim().isEmpty()) {
            android.util.Log.d("ProductDetail", "Loading image from URL: " + imageUrl);
            // Ensure ImageView is visible before loading
            ivProductImage.post(new Runnable() {
                @Override
                public void run() {
                    ivProductImage.setVisibility(View.VISIBLE);
                    ivProductImage.setAlpha(1.0f);
                    // Use ImageLoader to load image
                    com.example.riotshop.utils.ImageLoader.loadImage(ivProductImage, imageUrl);
                }
            });
        } else {
            android.util.Log.w("ProductDetail", "Image URL is null or empty, using placeholder");
            ivProductImage.post(new Runnable() {
                @Override
                public void run() {
                    ivProductImage.setImageResource(R.drawable.placeholder_account);
                    ivProductImage.setVisibility(View.VISIBLE);
                    ivProductImage.setAlpha(1.0f);
                }
            });
        }
        
        // Display inventory quantity
        if (tvProductQuantity != null) {
            if (product.getInventory() != null) {
                int quantity = product.getInventory().getQuantityAvailable();
                if (quantity > 0) {
                    tvProductQuantity.setText("Còn lại: " + quantity + " tài khoản");
                    tvProductQuantity.setTextColor(ContextCompat.getColor(this, R.color.riot_text_secondary));
                } else {
                    tvProductQuantity.setText("Hết hàng");
                    tvProductQuantity.setTextColor(ContextCompat.getColor(this, R.color.error));
                }
                tvProductQuantity.setVisibility(View.VISIBLE);
            } else {
                tvProductQuantity.setVisibility(View.GONE);
            }
        }
    }
    
    private void loadRelatedProducts() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<ProductTemplate>>> call = apiService.getRelatedProducts(templateId, 5);
        
        call.enqueue(new Callback<ApiResponse<List<ProductTemplate>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ProductTemplate>>> call, Response<ApiResponse<List<ProductTemplate>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ProductTemplate>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        relatedProducts.clear();
                        for (ProductTemplate template : apiResponse.getData()) {
                            Product product = new Product(
                                    template.getTemplateId(),
                                    template.getTitle(),
                                    FormatUtils.formatPrice(template.getBasePrice()),
                                    R.drawable.placeholder_account,
                                    template.getImageUrl(),
                                    template.getDescription() != null ? template.getDescription() : ""
                            );
                            relatedProducts.add(product);
                        }
                        if (relatedProductAdapter != null) {
                            relatedProductAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ProductTemplate>>> call, Throwable t) {
                // Silent fail for related products
            }
        });
    }
    
    private void setupRelatedProductsRecyclerView() {
        // RecyclerView not in layout - method kept for future use
    }
    
    private void setupReviewsRecyclerView() {
        // RecyclerView not in layout - method kept for future use
    }

    private void addToCart() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sử dụng templateId từ intent thay vì currentProduct để tránh bug
        if (templateId <= 0) {
            Toast.makeText(this, "Đang tải thông tin sản phẩm...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnAddToCart.setEnabled(false);
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        AddToCartRequest request = new AddToCartRequest(templateId, 1);
        Call<ApiResponse<com.example.riotshop.models.CartItem>> call = apiService.addToCart("Bearer " + token, request);
        
        call.enqueue(new Callback<ApiResponse<com.example.riotshop.models.CartItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.riotshop.models.CartItem>> call, Response<ApiResponse<com.example.riotshop.models.CartItem>> response) {
                btnAddToCart.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.riotshop.models.CartItem>> call, Throwable t) {
                btnAddToCart.setEnabled(true);
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void buyNow(int productTemplateId) {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnBuyNow.setEnabled(false);
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        AddToCartRequest request = new AddToCartRequest(productTemplateId, 1);
        Call<ApiResponse<com.example.riotshop.models.CartItem>> call = apiService.addToCart("Bearer " + token, request);
        
        call.enqueue(new Callback<ApiResponse<com.example.riotshop.models.CartItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.riotshop.models.CartItem>> call, Response<ApiResponse<com.example.riotshop.models.CartItem>> response) {
                btnBuyNow.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Thêm vào giỏ hàng thành công, chuyển đến trang giỏ hàng
                    Intent intent = new Intent(ProductDetailActivity.this, com.example.riotshop.ui.cart.CartActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.riotshop.models.CartItem>> call, Throwable t) {
                btnBuyNow.setEnabled(true);
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onItemClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("templateId", product.getTemplateId());
        startActivity(intent);
    }
    
    @Override
    public void onFavoriteClick(Product product, boolean isFavorite) {
        // Handle favorite click from related products
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        
        if (isFavorite) {
            // Need to find wishlistId first - simplified version
            Toast.makeText(this, "Vui lòng vào trang chi tiết để xóa yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            AddWishlistRequest request = new AddWishlistRequest(product.getTemplateId());
            Call<ApiResponse<Wishlist>> call = apiService.addToWishlist("Bearer " + token, request);
            call.enqueue(new Callback<ApiResponse<Wishlist>>() {
                @Override
                public void onResponse(Call<ApiResponse<Wishlist>> call, Response<ApiResponse<Wishlist>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ProductDetailActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                        // Reload related products to update favorite icons
                        loadRelatedProducts();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Wishlist>> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
