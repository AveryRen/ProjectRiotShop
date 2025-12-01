package com.example.riotshop.ui.other;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.ConfirmPaymentRequest;
import com.example.riotshop.models.CreatePaymentIntentRequest;
import com.example.riotshop.models.UserResponse;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DepositActivity extends AppCompatActivity {

    private TextInputEditText etAmount;
    private Button btnDeposit, btnAmount10, btnAmount25, btnAmount50, btnAmount100, btnTransactionHistory;
    private TextView tvCurrentBalance;
    private ProgressBar progressBar;
    private PaymentSheet paymentSheet;
    private String currentClientSecret;
    private String currentPaymentIntentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        SharedPrefManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        setupListeners();
        loadCurrentBalance();
        
        // Initialize Stripe PaymentSheet
        // Note: You need to set publishable key from backend or config
        // For now, we'll get it from the payment intent response
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
    }

    private void initViews() {
        etAmount = findViewById(R.id.et_amount);
        btnDeposit = findViewById(R.id.btn_deposit);
        btnAmount10 = findViewById(R.id.btn_amount_10);
        btnAmount25 = findViewById(R.id.btn_amount_25);
        btnAmount50 = findViewById(R.id.btn_amount_50);
        btnAmount100 = findViewById(R.id.btn_amount_100);
        btnTransactionHistory = findViewById(R.id.btn_transaction_history);
        tvCurrentBalance = findViewById(R.id.tv_current_balance);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnAmount10.setOnClickListener(v -> etAmount.setText("50000"));
        btnAmount25.setOnClickListener(v -> etAmount.setText("100000"));
        btnAmount50.setOnClickListener(v -> etAmount.setText("250000"));
        btnAmount100.setOnClickListener(v -> etAmount.setText("500000"));

        btnDeposit.setOnClickListener(v -> handleDeposit());
        btnTransactionHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void loadCurrentBalance() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<UserResponse>> call = apiService.getCurrentUser("Bearer " + token);

        call.enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse user = response.body().getData();
                    if (user != null) {
                        // Balance is already in VND, FormatUtils already adds "VNĐ"
                        tvCurrentBalance.setText(FormatUtils.formatPrice(user.getBalance()));
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                // Silent fail, just show 0
                tvCurrentBalance.setText(FormatUtils.formatPrice(0));
            }
        });
    }

    private void handleDeposit() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // VND validation: minimum 10,000 VND, maximum 25,000,000 VND
        if (amount < 10000) {
            Toast.makeText(this, "Số tiền tối thiểu là 10.000₫", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount > 25000000) {
            Toast.makeText(this, "Số tiền tối đa là 25.000.000₫", Toast.LENGTH_SHORT).show();
            return;
        }

        createPaymentIntent(amount);
    }

    private void createPaymentIntent(double amount) {
        showProgress(true);
        btnDeposit.setEnabled(false);

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            showProgress(false);
            btnDeposit.setEnabled(true);
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        CreatePaymentIntentRequest request = new CreatePaymentIntentRequest(amount, "VND");
        Call<ApiResponse<Object>> call = apiService.createPaymentIntent("Bearer " + token, request);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                showProgress(false);
                btnDeposit.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    try {
                        // Parse response to get client secret and payment intent ID
                        // The response.data should be a JSON object with clientSecret and paymentIntentId
                        String jsonString = new com.google.gson.Gson().toJson(response.body().getData());
                        JSONObject jsonObject = new JSONObject(jsonString);
                        
                        String clientSecret = jsonObject.getString("clientSecret");
                        currentPaymentIntentId = jsonObject.getString("paymentIntentId");
                        
                        // Get publishable key - TODO: Get from backend API or config
                        // For now, you need to set your Stripe publishable key here
                        String publishableKey = "pk_test_51SZWoHDE2GFDmMpILDKOivOgP2MFXd9ih2XlSTJIU3KZxq7M2KHzhwkYFbbsMoKhL2WUL2F6TdLW6VZvap867zPU00jHfYmNzH";
                        
                        PaymentConfiguration.init(DepositActivity.this, publishableKey);
                        
                        // Present payment sheet
                        presentPaymentSheet(clientSecret);
                        
                    } catch (Exception e) {
                        Toast.makeText(DepositActivity.this, "Lỗi xử lý thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Lỗi tạo thanh toán";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(DepositActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                showProgress(false);
                btnDeposit.setEnabled(true);
                Toast.makeText(DepositActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void presentPaymentSheet(String clientSecret) {
        currentClientSecret = clientSecret;
        
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("RiotShop")
            .build();
        
        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            configuration
        );
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Payment succeeded, confirm with backend
            confirmPayment(currentPaymentIntentId);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Thanh toán đã bị hủy", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failed = (PaymentSheetResult.Failed) paymentSheetResult;
            Toast.makeText(this, "Thanh toán thất bại: " + failed.getError().getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmPayment(String paymentIntentId) {
        showProgress(true);

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        ConfirmPaymentRequest request = new ConfirmPaymentRequest(paymentIntentId);
        Call<ApiResponse<Object>> call = apiService.confirmPayment("Bearer " + token, request);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                showProgress(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(DepositActivity.this, "Nạp tiền thành công!", Toast.LENGTH_SHORT).show();
                    // Reload balance
                    loadCurrentBalance();
                    // Clear amount field
                    etAmount.setText("");
                } else {
                    String errorMsg = "Lỗi xác nhận thanh toán";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(DepositActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                showProgress(false);
                Toast.makeText(DepositActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnDeposit.setEnabled(!show);
    }
}

