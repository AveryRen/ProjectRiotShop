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
import com.example.riotshop.models.Review;
import com.example.riotshop.models.Wishlist;
import com.example.riotshop.ui.comment.AddCommentActivity;
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
        tvProductDescription.setText(product.getDescription() != null ? product.getDescription() : "");
        
        // Load image from URL if available, otherwise use placeholder
        String imageUrl = product.getImageUrl();
        android.util.Log.d("ProductDetail", "Product imageUrl: " + imageUrl);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            android.util.Log.d("ProductDetail", "Loading image from URL: " + imageUrl);
            com.example.riotshop.utils.ImageLoader.loadImage(ivProductImage, imageUrl);
        } else {
            android.util.Log.w("ProductDetail", "Image URL is null or empty, using placeholder");
            ivProductImage.setImageResource(R.drawable.placeholder_account);
        }
        
        // Display inventory quantity
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
                        relatedProductAdapter.notifyDataSetChanged();
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
        relatedProductAdapter = new ProductAdapter(this, relatedProducts);
        relatedProductAdapter.setOnItemClickListener(this);
        relatedProductAdapter.setOnFavoriteClickListener(this);
        rvRelatedProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRelatedProducts.setAdapter(relatedProductAdapter);
    }
    
    private void setupReviewsRecyclerView() {
        int currentUserId = SharedPrefManager.getInstance(this).getUserId();
        reviewAdapter = new ReviewAdapter(this, reviews, currentUserId);
        reviewAdapter.setOnReviewActionListener(new ReviewAdapter.OnReviewActionListener() {
            @Override
            public void onEditReview(Review review) {
                // Open AddCommentActivity in edit mode
                Intent intent = new Intent(ProductDetailActivity.this, AddCommentActivity.class);
                intent.putExtra("templateId", templateId);
                intent.putExtra("reviewId", review.getReviewId());
                intent.putExtra("rating", review.getRating());
                intent.putExtra("comment", review.getComment());
                startActivityForResult(intent, 100);
            }

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
