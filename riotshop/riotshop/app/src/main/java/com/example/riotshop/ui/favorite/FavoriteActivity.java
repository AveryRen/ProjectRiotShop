package com.example.riotshop.ui.favorite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.adapters.ProductAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.Product;
import com.example.riotshop.models.ProductTemplate;
import com.example.riotshop.models.Wishlist;
import com.example.riotshop.ui.product.ProductDetailActivity;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener, ProductAdapter.OnFavoriteClickListener {

    private RecyclerView rvWishlist;
    private ProductAdapter productAdapter;
    private List<Product> wishlistProducts;
    private TextView tvEmptyWishlist;
    private Toolbar toolbar;
    private java.util.Map<Integer, Integer> wishlistIdMap; // Map templateId to wishlistId
    private java.util.Set<Integer> favoriteTemplateIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        SharedPrefManager.getInstance(this);

        toolbar = findViewById(R.id.toolbar_favorite);
        rvWishlist = findViewById(R.id.rv_wishlist);
        tvEmptyWishlist = findViewById(R.id.tv_empty_wishlist);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Yêu thích");

        wishlistProducts = new ArrayList<>();
        wishlistIdMap = new HashMap<>();
        favoriteTemplateIds = new HashSet<>();
        setupRecyclerView();
        loadWishlist();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, wishlistProducts);
        productAdapter.setOnItemClickListener(this);
        productAdapter.setOnFavoriteClickListener(this);
        rvWishlist.setLayoutManager(new GridLayoutManager(this, 2));
        rvWishlist.setAdapter(productAdapter);
    }

    private void loadWishlist() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            showEmptyWishlist();
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Wishlist>>> call = apiService.getWishlist("Bearer " + token);

        call.enqueue(new Callback<ApiResponse<List<Wishlist>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Wishlist>>> call, Response<ApiResponse<List<Wishlist>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Wishlist>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        wishlistProducts.clear();
                        wishlistIdMap.clear();
                        favoriteTemplateIds.clear();
                        for (Wishlist wishlist : apiResponse.getData()) {
                            if (wishlist.getProductTemplate() != null) {
                                ProductTemplate template = wishlist.getProductTemplate();
                                Product product = new Product(
                                        template.getTemplateId(),
                                        template.getTitle(),
                                        FormatUtils.formatPrice(template.getBasePrice()),
                                        R.drawable.placeholder_account,
                                        template.getImageUrl(),
                                        template.getDescription() != null ? template.getDescription() : ""
                                );
                                wishlistProducts.add(product);
                                wishlistIdMap.put(template.getTemplateId(), wishlist.getWishlistId());
                                favoriteTemplateIds.add(template.getTemplateId());
                            }
                        }
                        productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
                        productAdapter.notifyDataSetChanged();
                        showWishlist();
                    } else {
                        showEmptyWishlist();
                    }
                } else {
                    showEmptyWishlist();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Wishlist>>> call, Throwable t) {
                Toast.makeText(FavoriteActivity.this, "Lỗi khi tải danh sách yêu thích: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyWishlist();
            }
        });
    }

    private void showEmptyWishlist() {
        tvEmptyWishlist.setVisibility(View.VISIBLE);
        rvWishlist.setVisibility(View.GONE);
    }

    private void showWishlist() {
        tvEmptyWishlist.setVisibility(View.GONE);
        rvWishlist.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("templateId", product.getTemplateId());
        startActivity(intent);
    }
    
    @Override
    public void onFavoriteClick(Product product, boolean isFavorite) {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        
        if (isFavorite) {
            // Remove from wishlist (unlike)
            Integer wishlistId = wishlistIdMap.get(product.getTemplateId());
            if (wishlistId != null) {
                Call<ApiResponse<Object>> call = apiService.removeFromWishlist("Bearer " + token, wishlistId);
                call.enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // Remove from list immediately
                            wishlistProducts.remove(product);
                            wishlistIdMap.remove(product.getTemplateId());
                            favoriteTemplateIds.remove(product.getTemplateId());
                            productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
                            productAdapter.notifyDataSetChanged();
                            
                            if (wishlistProducts.isEmpty()) {
                                showEmptyWishlist();
                            }
                            
                            Toast.makeText(FavoriteActivity.this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FavoriteActivity.this, "Lỗi khi xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(FavoriteActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // Add to wishlist (like) - should not happen in favorite page, but handle it anyway
            com.example.riotshop.models.AddWishlistRequest request = new com.example.riotshop.models.AddWishlistRequest(product.getTemplateId());
            Call<ApiResponse<Wishlist>> call = apiService.addToWishlist("Bearer " + token, request);
            call.enqueue(new Callback<ApiResponse<Wishlist>>() {
                @Override
                public void onResponse(Call<ApiResponse<Wishlist>> call, Response<ApiResponse<Wishlist>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Wishlist wishlist = response.body().getData();
                        wishlistIdMap.put(product.getTemplateId(), wishlist.getWishlistId());
                        favoriteTemplateIds.add(product.getTemplateId());
                        productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
                        Toast.makeText(FavoriteActivity.this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Wishlist>> call, Throwable t) {
                    Toast.makeText(FavoriteActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload wishlist when activity resumes to sync with other pages
        loadWishlist();
    }
}
