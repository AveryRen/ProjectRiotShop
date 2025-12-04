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

    private RecyclerView rvFavoriteProducts;
    private ProductAdapter productAdapter;
    private List<Product> wishlistProducts;
    private TextView tvEmptyWishlist; // You might want to add this view to your layout
    private Toolbar toolbar;
    private Map<Integer, Integer> wishlistIdMap;
    private Set<Integer> favoriteTemplateIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        SharedPrefManager.getInstance(this);

        toolbar = findViewById(R.id.toolbar_favorite);
        rvFavoriteProducts = findViewById(R.id.rv_favorite_products); // Corrected ID
        // tvEmptyWishlist = findViewById(R.id.tv_empty_wishlist); // Add this if you have it

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        rvFavoriteProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvFavoriteProducts.setAdapter(productAdapter);
    }

    private void loadWishlist() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            showEmptyWishlist(true);
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Wishlist>>> call = apiService.getWishlist("Bearer " + token);

        call.enqueue(new Callback<ApiResponse<List<Wishlist>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Wishlist>>> call, Response<ApiResponse<List<Wishlist>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Wishlist> loadedItems = response.body().getData();
                    wishlistProducts.clear();
                    wishlistIdMap.clear();
                    favoriteTemplateIds.clear();

                    if (loadedItems != null && !loadedItems.isEmpty()) {
                        for (Wishlist wishlist : loadedItems) {
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
                    }
                    productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
                    productAdapter.notifyDataSetChanged();
                    showEmptyWishlist(wishlistProducts.isEmpty());
                } else {
                    showEmptyWishlist(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Wishlist>>> call, Throwable t) {
                Toast.makeText(FavoriteActivity.this, "Lỗi khi tải danh sách yêu thích: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyWishlist(true);
            }
        });
    }

    private void showEmptyWishlist(boolean isEmpty) {
        if (isEmpty) {
            rvFavoriteProducts.setVisibility(View.GONE);
            // if (tvEmptyWishlist != null) tvEmptyWishlist.setVisibility(View.VISIBLE);
        } else {
            rvFavoriteProducts.setVisibility(View.VISIBLE);
            // if (tvEmptyWishlist != null) tvEmptyWishlist.setVisibility(View.GONE);
        }
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

        if (isFavorite) {
            Integer wishlistId = wishlistIdMap.get(product.getTemplateId());
            if (wishlistId != null) {
                ApiService apiService = RetrofitClient.getInstance().getApiService();
                Call<ApiResponse<Object>> call = apiService.removeFromWishlist("Bearer " + token, wishlistId);
                call.enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            int position = -1;
                            for (int i = 0; i < wishlistProducts.size(); i++) {
                                if (wishlistProducts.get(i).getTemplateId() == product.getTemplateId()) {
                                    position = i;
                                    break;
                                }
                            }
                            if (position != -1) {
                                wishlistProducts.remove(position);
                                productAdapter.notifyItemRemoved(position);
                            }
                            wishlistIdMap.remove(product.getTemplateId());
                            favoriteTemplateIds.remove(product.getTemplateId());
                            productAdapter.setFavoriteTemplateIds(favoriteTemplateIds); // Update the set in adapter
                            showEmptyWishlist(wishlistProducts.isEmpty());
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
        loadWishlist();
    }
}
