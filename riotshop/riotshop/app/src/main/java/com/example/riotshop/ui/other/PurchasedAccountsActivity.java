package com.example.riotshop.ui.other;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.adapters.PurchasedAccountAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.PurchasedAccount;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PurchasedAccountsActivity extends AppCompatActivity implements PurchasedAccountAdapter.OnAccountClickListener {

    private Toolbar toolbar;
    private RecyclerView rvPurchasedAccounts;
    private TextView tvEmptyAccounts;
    private PurchasedAccountAdapter adapter;
    private List<PurchasedAccount> purchasedAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchased_accounts);

        SharedPrefManager.getInstance(this);

        toolbar = findViewById(R.id.toolbar_purchased_accounts);
        rvPurchasedAccounts = findViewById(R.id.rv_purchased_accounts);
        tvEmptyAccounts = findViewById(R.id.tv_empty_accounts);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tài khoản đã mua");

        purchasedAccounts = new ArrayList<>();
        setupRecyclerView();
        loadPurchasedAccounts();
    }

    private void setupRecyclerView() {
        adapter = new PurchasedAccountAdapter(this, purchasedAccounts, this);
        rvPurchasedAccounts.setLayoutManager(new LinearLayoutManager(this));
        rvPurchasedAccounts.setAdapter(adapter);
    }

    private void loadPurchasedAccounts() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Object>>> call = apiService.getMyPurchasedAccounts("Bearer " + token);

        call.enqueue(new Callback<ApiResponse<List<Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Object>>> call, Response<ApiResponse<List<Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Object> rawAccounts = response.body().getData();
                    if (rawAccounts != null && !rawAccounts.isEmpty()) {
                        Gson gson = new Gson();
                        Type accountListType = new TypeToken<List<PurchasedAccount>>(){}.getType();
                        List<PurchasedAccount> parsedAccounts = gson.fromJson(gson.toJson(rawAccounts), accountListType);

                        purchasedAccounts.clear();
                        purchasedAccounts.addAll(parsedAccounts);
                        adapter.notifyDataSetChanged();
                        showAccounts();
                    } else {
                        showEmptyAccounts();
                    }
                } else {
                    showEmptyAccounts();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Object>>> call, Throwable t) {
                Toast.makeText(PurchasedAccountsActivity.this, "Lỗi khi tải tài khoản: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyAccounts();
            }
        });
    }

    private void showEmptyAccounts() {
        tvEmptyAccounts.setVisibility(View.VISIBLE);
        rvPurchasedAccounts.setVisibility(View.GONE);
    }

    private void showAccounts() {
        tvEmptyAccounts.setVisibility(View.GONE);
        rvPurchasedAccounts.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCopyUsername(PurchasedAccount account) {
        if (account.getAccountDetail() != null && account.getAccountDetail().getAccountUsername() != null) {
            copyToClipboard("Tên đăng nhập", account.getAccountDetail().getAccountUsername());
        }
    }

    @Override
    public void onCopyPassword(PurchasedAccount account) {
        if (account.getAccountDetail() != null && account.getAccountDetail().getAccountPassword() != null) {
            copyToClipboard("Mật khẩu", account.getAccountDetail().getAccountPassword());
        }
    }

    @Override
    public void onCopyRiotId(PurchasedAccount account) {
        if (account.getAccountDetail() != null && account.getAccountDetail().getRiotId() != null) {
            copyToClipboard("Riot ID", account.getAccountDetail().getRiotId());
        }
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Đã sao chép " + label, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPurchasedAccounts(); // Reload when activity resumes
    }
}

