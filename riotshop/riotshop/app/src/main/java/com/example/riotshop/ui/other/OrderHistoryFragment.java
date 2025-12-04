package com.example.riotshop.ui.other;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.adapters.OrderAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.Order;
import com.example.riotshop.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private TextView tvEmptyOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        rvOrders = view.findViewById(R.id.rv_order_history);
        tvEmptyOrders = view.findViewById(R.id.tv_empty_order_history);

        orderList = new ArrayList<>();
        
        // Check if RecyclerView exists before setting it up
        if (rvOrders != null) {
            setupRecyclerView();
            loadOrders();
        } else {
            android.util.Log.e("OrderHistoryFragment", "RecyclerView not found in layout!");
        }

        return view;
    }

    private void setupRecyclerView() {
        if (rvOrders == null) {
            android.util.Log.e("OrderHistoryFragment", "RecyclerView is null, cannot setup");
            return;
        }
        
        orderAdapter = new OrderAdapter(getContext(), orderList, order -> {
            Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            startActivity(intent);
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        if (token == null) {
            showEmptyOrders();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Order>>> call = apiService.getMyOrders("Bearer " + token);

        call.enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Order>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        orderList.clear();
                        orderList.addAll(apiResponse.getData());
                        orderAdapter.notifyDataSetChanged();
                        showOrders();
                    } else {
                        showEmptyOrders();
                    }
                } else {
                    showEmptyOrders();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi tải đơn hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyOrders();
            }
        });
    }

    private void showEmptyOrders() {
        if (tvEmptyOrders != null) {
            tvEmptyOrders.setVisibility(View.VISIBLE);
        }
        if (rvOrders != null) {
            rvOrders.setVisibility(View.GONE);
        }
    }

    private void showOrders() {
        if (tvEmptyOrders != null) {
            tvEmptyOrders.setVisibility(View.GONE);
        }
        if (rvOrders != null) {
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders(); // Reload when fragment resumes
    }
}
