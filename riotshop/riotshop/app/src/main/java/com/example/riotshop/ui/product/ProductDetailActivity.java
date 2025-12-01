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
import com.example.riotshop.models.Product;
import com.example.riotshop.models.ProductTemplate;
import com.example.riotshop.models.Review;
import com.example.riotshop.models.Wishlist;
import com.example.riotshop.ui.comment.AddCommentActivity;
import com.example.riotshop.ui.other.CheckoutActivity;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener, ProductAdapter.OnFavoriteClickListener {

    private ImageView ivProductImage;
    private ImageButton btnFavorite;
    private TextView tvProductName, tvProductPrice, tvProductDescription, tvNoReviews, tvProductQuantity;
    private Button btnBuyNow, btnAddToCart, btnAddReview;
    private Toolbar toolbar;
    private RecyclerView rvRelatedProducts, rvReviews;
    private ProductAdapter relatedProductAdapter;
    private ReviewAdapter reviewAdapter;
    private List<Product> relatedProducts;
    private List<Review> reviews;
    private ProductTemplate currentProduct;
    private int templateId;
    private boolean isFavorite = false;
    private Integer wishlistId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize SharedPrefManager
        SharedPrefManager.getInstance(this);

        // Ánh xạ Views
        ivProductImage = findViewById(R.id.iv_product_image_detail);
        tvProductName = findViewById(R.id.tv_product_name_detail);
        tvProductPrice = findViewById(R.id.tv_product_price_detail);
        tvProductDescription = findViewById(R.id.tv_product_description_detail);
        tvProductQuantity = findViewById(R.id.tv_product_quantity);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnAddReview = findViewById(R.id.btn_add_review);
        toolbar = findViewById(R.id.toolbar_product_detail);
        rvRelatedProducts = findViewById(R.id.rv_related_products);
        rvReviews = findViewById(R.id.rv_reviews);
        tvNoReviews = findViewById(R.id.tv_no_reviews);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Lấy templateId từ intent
        templateId = getIntent().getIntExtra("templateId", 0);
        
        relatedProducts = new ArrayList<>();
        reviews = new ArrayList<>();
        setupRelatedProductsRecyclerView();
        setupReviewsRecyclerView();
        
        if (templateId > 0) {
            loadProductDetail();
            loadRelatedProducts();
            loadReviews();
            checkFavoriteStatus();
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý sự kiện click
        btnBuyNow.setOnClickListener(v -> {
            if (currentProduct != null) {
                Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
                intent.putExtra("templateId", currentProduct.getTemplateId());
                intent.putExtra("quantity", 1);
                startActivity(intent);
            }
        });

        btnAddToCart.setOnClickListener(v -> addToCart());
        
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        
        btnAddReview.setOnClickListener(v -> {
            if (currentProduct != null) {
                Intent intent = new Intent(this, AddCommentActivity.class);
                intent.putExtra("templateId", currentProduct.getTemplateId());
                startActivityForResult(intent, 100);
            }
        });
    }
    
    private void loadProductDetail() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<ProductTemplate>> call = apiService.getProductById(templateId);
        
        call.enqueue(new Callback<ApiResponse<ProductTemplate>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductTemplate>> call, Response<ApiResponse<ProductTemplate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ProductTemplate> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        currentProduct = apiResponse.getData();
                        displayProduct(currentProduct);
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
        toolbar.setTitle(product.getTitle());
        tvProductName.setText(product.getTitle());
        tvProductPrice.setText(FormatUtils.formatPrice(product.getBasePrice()));
        tvProductDescription.setText(product.getDescription() != null ? product.getDescription() : "");
        ivProductImage.setImageResource(R.drawable.placeholder_account);
        
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

            @Override
            public void onDeleteReview(Review review) {
                // Show confirmation dialog
                new android.app.AlertDialog.Builder(ProductDetailActivity.this)
                    .setTitle("Xóa đánh giá")
                    .setMessage("Bạn có chắc chắn muốn xóa đánh giá này?")
                    .setPositiveButton("Xóa", (dialog, which) -> deleteReview(review.getReviewId()))
                    .setNegativeButton("Hủy", null)
                    .show();
            }
        });
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
    }
    
    private void deleteReview(int reviewId) {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.deleteReview("Bearer " + token, reviewId);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProductDetailActivity.this, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                    loadReviews(); // Reload reviews
                } else {
                    String errorMsg = "Lỗi khi xóa đánh giá";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void checkFavoriteStatus() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            btnFavorite.setVisibility(View.GONE);
            return;
        }
        
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Wishlist>>> call = apiService.getWishlist("Bearer " + token);
        
        call.enqueue(new Callback<ApiResponse<List<Wishlist>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Wishlist>>> call, Response<ApiResponse<List<Wishlist>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Wishlist> wishlists = response.body().getData();
                    if (wishlists != null) {
                        for (Wishlist wishlist : wishlists) {
                            if (wishlist.getTemplateId() == templateId) {
                                isFavorite = true;
                                wishlistId = wishlist.getWishlistId();
                                updateFavoriteIcon();
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Wishlist>>> call, Throwable t) {
                // Silent fail
            }
        });
    }
    
    private void toggleFavorite() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        
        if (isFavorite && wishlistId != null) {
            // Remove from wishlist
            Call<ApiResponse<Object>> call = apiService.removeFromWishlist("Bearer " + token, wishlistId);
            call.enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        isFavorite = false;
                        wishlistId = null;
                        updateFavoriteIcon();
                        Toast.makeText(ProductDetailActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add to wishlist
            AddWishlistRequest request = new AddWishlistRequest(templateId);
            Call<ApiResponse<Wishlist>> call = apiService.addToWishlist("Bearer " + token, request);
            call.enqueue(new Callback<ApiResponse<Wishlist>>() {
                @Override
                public void onResponse(Call<ApiResponse<Wishlist>> call, Response<ApiResponse<Wishlist>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        isFavorite = true;
                        wishlistId = response.body().getData().getWishlistId();
                        updateFavoriteIcon();
                        Toast.makeText(ProductDetailActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Wishlist>> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_gold : R.drawable.ic_favorite_border_gold);
    }
    
    private void loadReviews() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Review>>> call = apiService.getReviewsByTemplate(templateId);
        
        call.enqueue(new Callback<ApiResponse<List<Review>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Review>>> call, Response<ApiResponse<List<Review>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Review> reviewList = response.body().getData();
                    if (reviewList != null && !reviewList.isEmpty()) {
                        reviews.clear();
                        reviews.addAll(reviewList); // Show all reviews (auto approved)
                        reviewAdapter.notifyDataSetChanged();
                        hideNoReviews();
                    } else {
                        showNoReviews();
                    }
                } else {
                    showNoReviews();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Review>>> call, Throwable t) {
                showNoReviews();
            }
        });
    }
    
    private void showNoReviews() {
        tvNoReviews.setVisibility(View.VISIBLE);
        rvReviews.setVisibility(View.GONE);
    }
    
    private void hideNoReviews() {
        tvNoReviews.setVisibility(View.GONE);
        rvReviews.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorite status when activity resumes to sync with other pages
        checkFavoriteStatus();
        // Reload reviews to get latest approved reviews
        loadReviews();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            // Reload reviews after adding a new one (even if cancelled, in case it was added)
            loadReviews();
        }
    }
    
    private void addToCart() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentProduct == null) {
            Toast.makeText(this, "Đang tải thông tin sản phẩm...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnAddToCart.setEnabled(false);
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        AddToCartRequest request = new AddToCartRequest(currentProduct.getTemplateId(), 1);
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
        // Xử lý nút back trên Toolbar
        onBackPressed();
        return true;
    }
}
