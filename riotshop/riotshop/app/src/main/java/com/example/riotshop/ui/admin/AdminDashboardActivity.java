package com.example.riotshop.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.ui.auth.LoginActivity;
import com.example.riotshop.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView rvAdminFunctions;
    private Button btnLogout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize SharedPrefManager
        SharedPrefManager.getInstance(this);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        // Setup RecyclerView for admin functions
        rvAdminFunctions = findViewById(R.id.rv_admin_functions);
        setupAdminFunctions();

        // Logout button
        btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(AdminDashboardActivity.this).logout();
            Toast.makeText(AdminDashboardActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupAdminFunctions() {
        List<AdminFunction> functions = new ArrayList<>();
        functions.add(new AdminFunction("Quản Lý Sản Phẩm", R.drawable.ic_menu_gold, () -> {
            startActivity(new Intent(this, AdminProductListActivity.class));
        }));
        functions.add(new AdminFunction("Quản Lý Đơn Hàng", R.drawable.ic_history_gold, () -> {
            startActivity(new Intent(this, AdminOrderListActivity.class));
        }));
        functions.add(new AdminFunction("Quản Lý Người Dùng", R.drawable.ic_account_gold, () -> {
            startActivity(new Intent(this, AdminUserListActivity.class));
        }));
        functions.add(new AdminFunction("Thống Kê", R.drawable.ic_deposit_gold, () -> {
            startActivity(new Intent(this, AdminStatisticsActivity.class));
        }));

        AdminFunctionAdapter adapter = new AdminFunctionAdapter(functions);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvAdminFunctions.setLayoutManager(layoutManager);
        rvAdminFunctions.setAdapter(adapter);
        
        // Add spacing between items
        int spacing = getResources().getDimensionPixelSize(R.dimen.admin_function_spacing);
        rvAdminFunctions.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));
    }

    // Inner class for Admin Function
    public static class AdminFunction {
        private String title;
        private int iconRes;
        private Runnable onClick;

        public AdminFunction(String title, int iconRes, Runnable onClick) {
            this.title = title;
            this.iconRes = iconRes;
            this.onClick = onClick;
        }

        public String getTitle() {
            return title;
        }

        public int getIconRes() {
            return iconRes;
        }

        public Runnable getOnClick() {
            return onClick;
        }
    }
}
