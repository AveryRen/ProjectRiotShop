package com.example.riotshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.riotshop.R;
import com.example.riotshop.ui.auth.LoginActivity;
import com.example.riotshop.ui.cart.CartFragment;
import com.example.riotshop.ui.favorite.FavoriteActivity;
import com.example.riotshop.ui.other.DepositActivity;
import com.example.riotshop.ui.other.OrderHistoryFragment;
import com.example.riotshop.ui.other.PurchasedAccountsActivity;
import com.example.riotshop.ui.profile.ProfileFragment;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.android.material.navigation.NavigationView;
import android.view.View;
import android.widget.TextView;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.UserResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_header_desc, R.string.nav_header_desc);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        
        // Load và hiển thị thông tin user trong navigation header
        loadUserInfoToHeader(navigationView);

        // Load the default fragment
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Handle back press using the new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    // If not handled, the default back action will be taken
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_cart) {
            selectedFragment = new CartFragment();
        } else if (itemId == R.id.nav_favorite) {
            Intent favoriteIntent = new Intent(this, FavoriteActivity.class);
            startActivity(favoriteIntent);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_history) {
            selectedFragment = new OrderHistoryFragment();
        } else if (itemId == R.id.nav_purchased_accounts) {
            Intent purchasedIntent = new Intent(this, PurchasedAccountsActivity.class);
            startActivity(purchasedIntent);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else if (itemId == R.id.nav_deposit) {
            Intent depositIntent = new Intent(this, DepositActivity.class);
            startActivity(depositIntent);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
            return true;
        }

        if (selectedFragment != null) {
            replaceFragment(selectedFragment);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleLogout() {
        SharedPrefManager.getInstance(this).logout();
        Toast.makeText(this, "Đăng Xuất thành công", Toast.LENGTH_SHORT).show();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    
    private void loadUserInfoToHeader(NavigationView navigationView) {
        // Lấy header view từ NavigationView
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;
        
        TextView tvUsername = headerView.findViewById(R.id.tv_username_header);
        TextView tvEmail = headerView.findViewById(R.id.tv_email_header);
        
        // Load từ SharedPref trước (nhanh)
        if (tvUsername != null) {
            String username = SharedPrefManager.getInstance(this).getUsername();
            tvUsername.setText(username != null && !username.isEmpty() && !username.equals("Khách") ? username : "Tên Người Dùng");
        }
        
        if (tvEmail != null) {
            String email = SharedPrefManager.getInstance(this).getEmail();
            tvEmail.setText(email != null && !email.isEmpty() ? email : "email@example.com");
        }
        
        // Load từ API để cập nhật thông tin mới nhất
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token != null) {
            ApiService apiService = RetrofitClient.getInstance().getApiService();
            Call<ApiResponse<UserResponse>> call = apiService.getCurrentUser("Bearer " + token);
            
            call.enqueue(new Callback<ApiResponse<UserResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        UserResponse user = response.body().getData();
                        if (user != null) {
                            // Cập nhật UI
                            if (tvUsername != null && user.getUsername() != null) {
                                tvUsername.setText(user.getUsername());
                            }
                            if (tvEmail != null && user.getEmail() != null) {
                                tvEmail.setText(user.getEmail());
                            }
                            
                            // Cập nhật SharedPref để lần sau load nhanh hơn
                            SharedPrefManager.getInstance(HomeActivity.this).saveUserLogin(
                                new com.example.riotshop.models.User(
                                    user.getUsername(),
                                    user.getEmail()
                                )
                            );
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                    // Silent fail - giữ nguyên giá trị từ SharedPref
                }
            });
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload user info khi quay lại activity
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            loadUserInfoToHeader(navigationView);
        }
    }
}
