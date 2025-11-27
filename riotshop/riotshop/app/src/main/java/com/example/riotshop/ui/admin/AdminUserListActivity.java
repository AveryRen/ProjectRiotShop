package com.example.riotshop.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
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
import com.google.gson.Gson;

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

    private void setupRecyclerView() {
        userAdapter = new AdminUserAdapter(this, userList);
        userAdapter.setOnUserClickListener(this);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);
    }
    
    @Override
    public void onUserClick(UserResponse user) {
        // TODO: Navigate to user detail or show user info dialog
        Toast.makeText(this, "User: " + user.getUsername(), Toast.LENGTH_SHORT).show();
    }

    private void loadUsers() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Object>>> call = apiService.getAdminUsers("Bearer " + token, 1, 20);
        
        call.enqueue(new Callback<ApiResponse<List<Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Object>>> call, Response<ApiResponse<List<Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Object> users = response.body().getData();
                    if (users != null && !users.isEmpty()) {
                        // Parse Object list to UserResponse list using Gson
                        userList.clear();
                        Gson gson = new Gson();
                        for (Object obj : users) {
                            try {
                                String json = gson.toJson(obj);
                                UserResponse user = gson.fromJson(json, UserResponse.class);
                                userList.add(user);
                            } catch (Exception e) {
                                // Skip invalid users
                            }
                        }
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
                    Toast.makeText(AdminUserListActivity.this, "Lỗi khi tải người dùng: " + response.message(), Toast.LENGTH_SHORT).show();
                    showEmptyUsers();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Object>>> call, Throwable t) {
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
