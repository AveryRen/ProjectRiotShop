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
import com.example.riotshop.adapters.AdminUserAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.UserResponse;
import com.example.riotshop.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserListActivity extends AppCompatActivity implements AdminUserAdapter.OnUserClickListener {

    private RecyclerView rvUsers;
    private TextView tvEmptyUsers;
    private AdminUserAdapter userAdapter;
    private List<UserResponse> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        SharedPrefManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        rvUsers = findViewById(R.id.rv_users);
        tvEmptyUsers = findViewById(R.id.tv_empty_users);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản Lý Người Dùng");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        userList = new ArrayList<>();
        setupRecyclerView();
        loadUsers();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadUsers(); // Reload when coming back from edit
    }

    private void setupRecyclerView() {
        userAdapter = new AdminUserAdapter(this, userList);
        userAdapter.setOnUserClickListener(this);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);
    }
    
    @Override
    public void onUserClick(UserResponse user) {
        // Navigate to edit user
        Intent intent = new Intent(this, AdminEditUserActivity.class);
        intent.putExtra("userId", user.getUserId());
        startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_add, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add) {
            startActivity(new Intent(this, AdminAddUserActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUsers() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<UserResponse>>> call = apiService.getAdminUsers("Bearer " + token, 1, 20);
        
        call.enqueue(new Callback<ApiResponse<List<UserResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserResponse>>> call, Response<ApiResponse<List<UserResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<UserResponse> users = response.body().getData();
                    if (users != null && !users.isEmpty()) {
                        userList.clear();
                        userList.addAll(users);
                        userAdapter.notifyDataSetChanged();
                        if (userList.isEmpty()) {
                            showEmptyUsers();
                        } else {
                            showUsers();
                        }
                    } else {
                        showEmptyUsers();
                    }
                } else {
                    String errorMsg = "Lỗi khi tải người dùng";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (response.code() == 403) {
                        errorMsg = "Bạn không có quyền truy cập";
                    }
                    Toast.makeText(AdminUserListActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    showEmptyUsers();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserResponse>>> call, Throwable t) {
                Toast.makeText(AdminUserListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyUsers();
            }
        });
    }

    private void showEmptyUsers() {
        tvEmptyUsers.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.GONE);
    }

    private void showUsers() {
        tvEmptyUsers.setVisibility(View.GONE);
        rvUsers.setVisibility(View.VISIBLE);
    }
}
