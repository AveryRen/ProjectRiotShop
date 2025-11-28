package com.example.riotshop.ui.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.adapters.OrderAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.Order;
import com.example.riotshop.ui.other.OrderDetailActivity;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrderListActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {

    private RecyclerView rvOrders;
    private TextView tvEmptyOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_list);

        SharedPrefManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        rvOrders = findViewById(R.id.rv_orders);
        tvEmptyOrders = findViewById(R.id.tv_empty_orders);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản Lý Đơn Hàng");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        orderList = new ArrayList<>();
        setupRecyclerView();
        loadOrders();
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, orderList, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
    }
    
    @Override
    public void onOrderClick(Order order) {
        // Navigate to admin order detail
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("orderId", order.getOrderId());
        startActivity(intent);
    }

    private void loadOrders() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Object>>> call = apiService.getAdminOrders("Bearer " + token, null, null, 1, 20);
        
        call.enqueue(new Callback<ApiResponse<List<Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Object>>> call, Response<ApiResponse<List<Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Object> orders = response.body().getData();
                    if (orders != null && !orders.isEmpty()) {
                        // Parse Object list to Order list using Gson
                        orderList.clear();
                        Gson gson = new Gson();
                        for (Object obj : orders) {
                            try {
                                String json = gson.toJson(obj);
                                Order order = gson.fromJson(json, Order.class);
                                orderList.add(order);
                            } catch (Exception e) {
                                // Skip invalid orders
                            }
                        }
                        orderAdapter.notifyDataSetChanged();
                        if (orderList.isEmpty()) {
                            showEmptyOrders();
                        } else {
                            showOrders();
                        }
                    } else {
                        showEmptyOrders();
                    }
                } else {
                    Toast.makeText(AdminOrderListActivity.this, "Lỗi khi tải đơn hàng: " + response.message(), Toast.LENGTH_SHORT).show();
                    showEmptyOrders();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Object>>> call, Throwable t) {
                Toast.makeText(AdminOrderListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyOrders();
            }
        });
    }

    private void showEmptyOrders() {
        tvEmptyOrders.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
    }

    private void showOrders() {
        tvEmptyOrders.setVisibility(View.GONE);
        rvOrders.setVisibility(View.VISIBLE);
    }
}
