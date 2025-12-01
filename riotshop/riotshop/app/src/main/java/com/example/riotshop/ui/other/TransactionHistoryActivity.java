package com.example.riotshop.ui.other;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.adapters.TransactionAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.PaymentTransaction;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionHistoryActivity extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private TextView tvEmptyTransactions;
    private ProgressBar progressBar;
    private TransactionAdapter transactionAdapter;
    private List<PaymentTransaction> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        SharedPrefManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        setupRecyclerView();
        loadTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions(); // Reload when coming back
    }

    private void initViews() {
        rvTransactions = findViewById(R.id.rv_transactions);
        tvEmptyTransactions = findViewById(R.id.tv_empty_transactions);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void loadTransactions() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showProgress(true);

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Object>>> call = apiService.getPaymentTransactions("Bearer " + token, 1, 50);

        call.enqueue(new Callback<ApiResponse<List<Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Object>>> call, Response<ApiResponse<List<Object>>> response) {
                showProgress(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Object> data = response.body().getData();
                    if (data != null && !data.isEmpty()) {
                        // Convert Object list to PaymentTransaction list
                        Gson gson = new Gson();
                        String json = gson.toJson(data);
                        Type listType = new TypeToken<List<PaymentTransaction>>(){}.getType();
                        List<PaymentTransaction> transactions = gson.fromJson(json, listType);

                        transactionList.clear();
                        transactionList.addAll(transactions);
                        transactionAdapter.notifyDataSetChanged();
                        showTransactions();
                    } else {
                        showEmptyTransactions();
                    }
                } else {
                    String errorMsg = "Lỗi khi tải lịch sử giao dịch";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(TransactionHistoryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    showEmptyTransactions();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Object>>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(TransactionHistoryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyTransactions();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvTransactions.setVisibility(show ? View.GONE : View.VISIBLE);
        tvEmptyTransactions.setVisibility(View.GONE);
    }

    private void showTransactions() {
        rvTransactions.setVisibility(View.VISIBLE);
        tvEmptyTransactions.setVisibility(View.GONE);
    }

    private void showEmptyTransactions() {
        rvTransactions.setVisibility(View.GONE);
        tvEmptyTransactions.setVisibility(View.VISIBLE);
    }
}

