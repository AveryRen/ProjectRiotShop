package com.example.riotshop.ui.address;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.adapters.AddressAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.UserAddress;
import com.example.riotshop.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvAddresses;
    private AddressAdapter addressAdapter;
    private List<UserAddress> addressList;
    private TextView tvEmptyAddresses;
    private Button btnAddAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        SharedPrefManager.getInstance(this);

        toolbar = findViewById(R.id.toolbar_address);
        rvAddresses = findViewById(R.id.rv_addresses);
        tvEmptyAddresses = findViewById(R.id.tv_empty_addresses);
        btnAddAddress = findViewById(R.id.btn_add_address);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Địa chỉ");

        addressList = new ArrayList<>();
        setupRecyclerView();

        btnAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAddressActivity.class);
            startActivity(intent);
        });

        loadAddresses();
    }

    private void setupRecyclerView() {
        addressAdapter = new AddressAdapter(this, addressList, address -> {
            // Handle address click - can be used to select address
        });
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(addressAdapter);
    }

    private void loadAddresses() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            showEmptyAddresses();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<UserAddress>>> call = apiService.getMyAddresses("Bearer " + token);

        call.enqueue(new Callback<ApiResponse<List<UserAddress>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserAddress>>> call, Response<ApiResponse<List<UserAddress>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<UserAddress>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        addressList.clear();
                        addressList.addAll(apiResponse.getData());
                        addressAdapter.notifyDataSetChanged();
                        showAddresses();
                    } else {
                        showEmptyAddresses();
                    }
                } else {
                    showEmptyAddresses();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserAddress>>> call, Throwable t) {
                Toast.makeText(AddressActivity.this, "Lỗi khi tải địa chỉ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyAddresses();
            }
        });
    }

    private void showEmptyAddresses() {
        tvEmptyAddresses.setVisibility(View.VISIBLE);
        rvAddresses.setVisibility(View.GONE);
    }

    private void showAddresses() {
        tvEmptyAddresses.setVisibility(View.GONE);
        rvAddresses.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAddresses(); // Reload when activity resumes
    }
}
