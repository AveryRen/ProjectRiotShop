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
import com.example.riotshop.ui.other.OrderHistoryFragment;
import com.example.riotshop.ui.profile.ProfileFragment;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.android.material.navigation.NavigationView;

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
        } else if (itemId == R.id.nav_history) {
            selectedFragment = new OrderHistoryFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else if (itemId == R.id.nav_deposit) {
            Toast.makeText(this, "Chức năng Nạp tiền đang được phát triển", Toast.LENGTH_SHORT).show();
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
}
