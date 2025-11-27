package com.example.riotshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.adapters.CategoryAdapter;
import com.example.riotshop.adapters.ProductAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.Category;
import com.example.riotshop.models.GameType;
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

public class HomeFragment extends Fragment implements ProductAdapter.OnItemClickListener, ProductAdapter.OnFavoriteClickListener {

    private RecyclerView rvProducts, rvCategories;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> productList;
    private List<Category> categoryList;
    private Set<Integer> favoriteTemplateIds;
    private java.util.Map<Integer, Integer> wishlistIdMap; // Map templateId to wishlistId
    private EditText etSearch;
    private ImageButton btnFilter;
    private Integer selectedGameId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvProducts = view.findViewById(R.id.rv_products_fragment);
        rvCategories = view.findViewById(R.id.rv_categories);
        etSearch = view.findViewById(R.id.et_search);
        btnFilter = view.findViewById(R.id.btn_filter);
        
        productList = new ArrayList<>();
        categoryList = new ArrayList<>();
        favoriteTemplateIds = new HashSet<>();
        wishlistIdMap = new HashMap<>();
        
        setupRecyclerView();
        setupCategories();
        setupSearch();
        setupFilter();
        loadProducts();
        loadCategories();
        loadWishlist();

        return view;
    }
    
    private void setupCategories() {
        categoryAdapter = new CategoryAdapter(getContext(), categoryList);
        categoryAdapter.setOnCategoryClickListener(category -> {
            // Filter products by category
            if (category.getId().equals("0")) {
                selectedGameId = null; // Show all
            } else {
                try {
                    selectedGameId = Integer.parseInt(category.getId());
                } catch (NumberFormatException e) {
                    selectedGameId = null;
                }
            }
            loadProducts();
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);
    }
    
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString().trim();
                if (searchQuery.isEmpty()) {
                    loadProducts();
                } else {
                    searchProducts(searchQuery);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupFilter() {
        btnFilter.setOnClickListener(v -> {
            FilterBottomSheet bottomSheet = new FilterBottomSheet();
            bottomSheet.setFilterListener(new FilterBottomSheet.FilterListener() {
                @Override
                public void onFilterApplied(Integer gameId, Boolean isFeatured, String priceRange, String sortBy) {
                    applyFilters(gameId, isFeatured, priceRange, sortBy);
                }

                @Override
                public void onFilterReset() {
                    selectedGameId = null;
                    categoryAdapter.setSelectedCategoryById("0"); // Reset to "Tất cả"
                    loadProducts();
                }
            });
            bottomSheet.show(getParentFragmentManager(), "FilterBottomSheet");
        });
    }
    
    private void applyFilters(Integer gameId, Boolean isFeatured, String priceRange, String sortBy) {
        selectedGameId = gameId;
        
        // Update category adapter to highlight the selected category
        if (gameId == null) {
            categoryAdapter.setSelectedCategoryById("0"); // "Tất cả"
        } else {
            categoryAdapter.setSelectedCategoryById(String.valueOf(gameId));
        }
        
        loadProductsWithFilters(gameId, isFeatured, priceRange, sortBy);
    }
    
    private void loadProductsWithFilters(Integer gameId, Boolean isFeatured, String priceRange, String sortBy) {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<ProductTemplate>>> call = apiService.getProducts(gameId, isFeatured, null);
        
        call.enqueue(new Callback<ApiResponse<List<ProductTemplate>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ProductTemplate>>> call, Response<ApiResponse<List<ProductTemplate>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ProductTemplate>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<ProductTemplate> templates = apiResponse.getData();
                        
                        // Apply price filter (client-side since API doesn't support it)
                        if (priceRange != null) {
                            templates = filterByPriceRange(templates, priceRange);
                        }
                        
                        // Apply sorting (client-side since API doesn't support it)
                        if (sortBy != null) {
                            templates = sortProducts(templates, sortBy);
                        }
                        
                        productList.clear();
                        for (ProductTemplate template : templates) {
                            Product product = convertToProduct(template);
                            productList.add(product);
                        }
                        productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải sản phẩm: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ProductTemplate>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private List<ProductTemplate> filterByPriceRange(List<ProductTemplate> templates, String priceRange) {
        List<ProductTemplate> filtered = new ArrayList<>();
        for (ProductTemplate template : templates) {
            double price = template.getBasePrice();
            if (priceRange.equals("low") && price < 100000) {
                filtered.add(template);
            } else if (priceRange.equals("medium") && price >= 100000 && price <= 500000) {
                filtered.add(template);
            } else if (priceRange.equals("high") && price > 500000) {
                filtered.add(template);
            }
        }
        return filtered;
    }
    
    private List<ProductTemplate> sortProducts(List<ProductTemplate> templates, String sortBy) {
        List<ProductTemplate> sorted = new ArrayList<>(templates);
        if (sortBy.equals("price_asc")) {
            sorted.sort((a, b) -> Double.compare(a.getBasePrice(), b.getBasePrice()));
        } else if (sortBy.equals("price_desc")) {
            sorted.sort((a, b) -> Double.compare(b.getBasePrice(), a.getBasePrice()));
        } else if (sortBy.equals("name_asc")) {
            sorted.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        }
        return sorted;
    }
    
    private void loadCategories() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<GameType>>> call = apiService.getGames();
        
        call.enqueue(new Callback<ApiResponse<List<GameType>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GameType>>> call, Response<ApiResponse<List<GameType>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<GameType>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        categoryList.clear();
                        // Add "Tất cả" category
                        categoryList.add(new Category("0", "Tất cả", R.drawable.ic_category_default));
                        // Add game types as categories
                        for (GameType gameType : apiResponse.getData()) {
                            categoryList.add(new Category(
                                    String.valueOf(gameType.getGameId()),
                                    gameType.getName(),
                                    R.drawable.ic_category_default
                            ));
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GameType>>> call, Throwable t) {
                // Fallback to default categories
                categoryList.clear();
                categoryList.add(new Category("0", "Tất cả", R.drawable.ic_category_default));
                categoryList.add(new Category("1", "Liên Minh", R.drawable.ic_category_default));
                categoryList.add(new Category("2", "Valorant", R.drawable.ic_category_default));
                categoryAdapter.notifyDataSetChanged();
            }
        });
    }
    
    private void searchProducts(String searchQuery) {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<ProductTemplate>>> call = apiService.getProducts(selectedGameId, null, searchQuery);
        
        call.enqueue(new Callback<ApiResponse<List<ProductTemplate>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ProductTemplate>>> call, Response<ApiResponse<List<ProductTemplate>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ProductTemplate>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        productList.clear();
                        for (ProductTemplate template : apiResponse.getData()) {
                            Product product = convertToProduct(template);
                            productList.add(product);
                        }
                        productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
                        productAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ProductTemplate>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tìm kiếm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(getContext(), productList);
        productAdapter.setOnItemClickListener(this);
        productAdapter.setOnFavoriteClickListener(this);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(productAdapter);
    }
    
    private void loadWishlist() {
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        if (token == null) {
            favoriteTemplateIds.clear();
            wishlistIdMap.clear();
            if (productAdapter != null) {
                productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
            }
            return; // User not logged in
        }
        
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Wishlist>>> call = apiService.getWishlist("Bearer " + token);
        
        call.enqueue(new Callback<ApiResponse<List<Wishlist>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Wishlist>>> call, Response<ApiResponse<List<Wishlist>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Wishlist> wishlists = response.body().getData();
                    if (wishlists != null) {
                        favoriteTemplateIds.clear();
                        wishlistIdMap.clear();
                        for (Wishlist wishlist : wishlists) {
                            favoriteTemplateIds.add(wishlist.getTemplateId());
                            wishlistIdMap.put(wishlist.getTemplateId(), wishlist.getWishlistId());
                        }
                        if (productAdapter != null) {
                            productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
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

    private void loadProducts() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<ProductTemplate>>> call = apiService.getProducts(selectedGameId, null, null);
        
        call.enqueue(new Callback<ApiResponse<List<ProductTemplate>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ProductTemplate>>> call, Response<ApiResponse<List<ProductTemplate>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ProductTemplate>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        productList.clear();
                        for (ProductTemplate template : apiResponse.getData()) {
                            Product product = convertToProduct(template);
                            productList.add(product);
                        }
                        productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải sản phẩm: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ProductTemplate>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Fallback to mock data if API fails
                createMockProductList();
            }
        });
    }

    private Product convertToProduct(ProductTemplate template) {
        String price = FormatUtils.formatPrice(template.getBasePrice());
        String description = template.getDescription() != null ? template.getDescription() : "";
        return new Product(
                template.getTemplateId(),
                template.getTitle(),
                price,
                R.drawable.placeholder_account,
                description
        );
    }

    private void createMockProductList() {
        productList.clear();
        String desc_lmht = "- Hơn 500 trang phục, bao gồm nhiều trang phục Tối Thượng và Thần Thoại.\n- Rank Bạch Kim IV, thông tin trắng, có thể thay đổi email và mật khẩu.";
        productList.add(new Product("Acc LMHT Full Skin Hiếm", "1.500.000 VNĐ", R.drawable.placeholder_account, desc_lmht));

        String desc_valorant = "- Rank Bất Tử, sở hữu nhiều skin vũ khí đắt giá từ các bundle mới nhất.\n- Thông tin đầy đủ, sẵn sàng cho việc thi đấu chuyên nghiệp.";
        productList.add(new Product("Acc Valorant Rank Bất Tử", "1.100.000 VNĐ", R.drawable.placeholder_account, desc_valorant));

        String desc_tft = "- Sở hữu tất cả các Linh Thú và Sân Đấu hiếm.\n- Rank Cao Thủ, thông tin trắng, phù hợp cho việc cày rank và stream.";
        productList.add(new Product("Acc TFT Full Tướng 3 Sao", "700.000 VNĐ", R.drawable.placeholder_account, desc_tft));
        productAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(Product product) {
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("templateId", product.getTemplateId());
        startActivity(intent);
    }
    
    @Override
    public void onFavoriteClick(Product product, boolean isFavorite) {
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        if (token == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
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
                            // Update UI immediately
                            favoriteTemplateIds.remove(product.getTemplateId());
                            wishlistIdMap.remove(product.getTemplateId());
                            productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
                            Toast.makeText(getContext(), "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // If wishlistId not found, reload wishlist to get it
                loadWishlist();
            }
        } else {
            // Add to wishlist (like)
            com.example.riotshop.models.AddWishlistRequest request = new com.example.riotshop.models.AddWishlistRequest(product.getTemplateId());
            Call<ApiResponse<Wishlist>> call = apiService.addToWishlist("Bearer " + token, request);
            call.enqueue(new Callback<ApiResponse<Wishlist>>() {
                @Override
                public void onResponse(Call<ApiResponse<Wishlist>> call, Response<ApiResponse<Wishlist>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        // Update UI immediately
                        Wishlist wishlist = response.body().getData();
                        favoriteTemplateIds.add(product.getTemplateId());
                        wishlistIdMap.put(product.getTemplateId(), wishlist.getWishlistId());
                        productAdapter.setFavoriteTemplateIds(favoriteTemplateIds);
                        Toast.makeText(getContext(), "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Wishlist>> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload wishlist when fragment resumes to sync with other pages
        loadWishlist();
    }
}
