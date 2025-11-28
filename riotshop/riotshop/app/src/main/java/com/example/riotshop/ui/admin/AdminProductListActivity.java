package com.example.riotshop.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.example.riotshop.ui.product.ProductDetailActivity;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductListActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener {

    private RecyclerView rvProducts;
    private TextView tvEmptyProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_list);

        SharedPrefManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        rvProducts = findViewById(R.id.rv_products);
        tvEmptyProducts = findViewById(R.id.tv_empty_products);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản Lý Sản Phẩm");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        productList = new ArrayList<>();
        setupRecyclerView();
        loadProducts();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, productList);
        productAdapter.setOnItemClickListener(this);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(productAdapter);
    }
    
    @Override
    public void onItemClick(Product product) {
        // Show options: Edit or Manage Accounts
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chọn thao tác")
            .setItems(new String[]{"Sửa sản phẩm", "Quản lý tài khoản"}, (dialog, which) -> {
                if (which == 0) {
                    Intent intent = new Intent(this, AdminEditProductActivity.class);
                    intent.putExtra("templateId", product.getTemplateId());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, AdminAccountListActivity.class);
                    intent.putExtra("templateId", product.getTemplateId());
                    startActivity(intent);
                }
            })
            .show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_add, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add) {
            startActivity(new Intent(this, AdminAddProductActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(); // Reload when coming back from edit
    }

    private void loadProducts() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<ProductTemplate>>> call = apiService.getProducts(null, null, null);
        
        call.enqueue(new Callback<ApiResponse<List<ProductTemplate>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ProductTemplate>>> call, Response<ApiResponse<List<ProductTemplate>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ProductTemplate> templates = response.body().getData();
                    if (templates != null && !templates.isEmpty()) {
                        productList.clear();
                        for (ProductTemplate template : templates) {
                            productList.add(convertToProduct(template));
                        }
                        productAdapter.notifyDataSetChanged();
                        showProducts();
                    } else {
                        showEmptyProducts();
                    }
                } else {
                    Toast.makeText(AdminProductListActivity.this, "Lỗi khi tải sản phẩm: " + response.message(), Toast.LENGTH_SHORT).show();
                    showEmptyProducts();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ProductTemplate>>> call, Throwable t) {
                Toast.makeText(AdminProductListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyProducts();
            }
        });
    }

    private Product convertToProduct(ProductTemplate template) {
        return new Product(
            template.getTemplateId(),
            template.getTitle(),
            FormatUtils.formatPrice(template.getBasePrice()),
            R.drawable.placeholder_account,
            template.getDescription()
        );
    }

    private void showEmptyProducts() {
        tvEmptyProducts.setVisibility(View.VISIBLE);
        rvProducts.setVisibility(View.GONE);
    }

    private void showProducts() {
        tvEmptyProducts.setVisibility(View.GONE);
        rvProducts.setVisibility(View.VISIBLE);
    }
}
