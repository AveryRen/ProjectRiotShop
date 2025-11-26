package com.example.riotshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.adapters.AccountAdapter;
import com.example.riotshop.adapters.CategoryAdapter;
import com.example.riotshop.data.DataSource;
import com.example.riotshop.models.Account;
import com.example.riotshop.models.Category;
import com.example.riotshop.ui.auth.LoginActivity;
import com.example.riotshop.ui.detail.DetailActivity;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements AccountAdapter.OnAccountListener, CategoryAdapter.OnCategoryClickListener, FilterBottomSheet.FilterListener {

    private RecyclerView rvCategories, rvProducts;
    private CategoryAdapter categoryAdapter;
    private AccountAdapter accountAdapter;
    private List<Category> categoryList;
    private List<Account> productList;
    private TextView tvNoProducts;
    private FloatingActionButton fabFilter;

    private DataSource dataSource;

    private String currentCategory = "Tất cả";
    private float currentMinPrice = 0;
    private float currentMaxPrice = Float.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);

        dataSource = new DataSource(this);
        dataSource.open();

        rvCategories = findViewById(R.id.rv_categories);
        rvProducts = findViewById(R.id.rv_products);
        tvNoProducts = findViewById(R.id.tv_no_products);
        fabFilter = findViewById(R.id.fab_filter);

        setupCategoryRecyclerView();
        setupProductRecyclerView();

        fabFilter.setOnClickListener(v -> {
            FilterBottomSheet filterBottomSheet = new FilterBottomSheet();
            // You can pass current filter values to the bottom sheet if needed
            filterBottomSheet.show(getSupportFragmentManager(), filterBottomSheet.getTag());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void setupCategoryRecyclerView() {
        categoryList = new ArrayList<>();
        categoryList.add(new Category("Tất cả", true));
        categoryList.add(new Category("VIP", false));
        categoryList.add(new Category("Smurf", false));
        categoryList.add(new Category("Giá rẻ", false));

        categoryAdapter = new CategoryAdapter(this, categoryList, this);
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupProductRecyclerView() {
        productList = new ArrayList<>();
        accountAdapter = new AccountAdapter(this, productList, this);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(accountAdapter);
    }

    private void loadProducts() {
        productList.clear();
        productList.addAll(dataSource.getFilteredAccounts(currentCategory, currentMinPrice, currentMaxPrice));
        updateProductUI();
    }

    private void updateProductUI() {
        if (productList.isEmpty()) {
            tvNoProducts.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            tvNoProducts.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
        accountAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            // Implement your logout logic
            // For example, clear shared preferences and go to login screen
            // SharedPrefManager.getInstance(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity(); // Close all activities
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccountClick(Account account) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("ACCOUNT_ID", account.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Account account) {
        // You need to get current user id from your session management
        String currentUserId = "1"; // Placeholder
        dataSource.addFavorite(currentUserId, account.getId());
        Toast.makeText(this, "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCategoryClick(Category category) {
        currentCategory = category.getName();
        loadProducts();
    }

    @Override
    public void onFilterApplied(String category, float minPrice, float maxPrice) {
        currentCategory = category;
        currentMinPrice = minPrice;
        currentMaxPrice = maxPrice;
        loadProducts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }
}
