package com.example.riotshop.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.UserResponse;
import com.example.riotshop.ui.other.PurchasedAccountsActivity;
import com.example.riotshop.ui.profile.EditProfileActivity;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail, tvFullName, tvPhone, tvBalance;
    private Button btnEditProfile, btnPurchasedAccounts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvBalance = view.findViewById(R.id.tv_balance);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnPurchasedAccounts = view.findViewById(R.id.btn_purchased_accounts);

        SharedPrefManager.getInstance(getContext());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        btnPurchasedAccounts.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PurchasedAccountsActivity.class);
            startActivity(intent);
        });

        loadUserProfile();

        return view;
    }

    private void loadUserProfile() {
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        if (token == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<UserResponse>> call = apiService.getCurrentUser("Bearer " + token);

        call.enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        UserResponse user = apiResponse.getData();
                        displayUserInfo(user);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi tải thông tin: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo(UserResponse user) {
        tvUsername.setText(user.getUsername() != null ? user.getUsername() : "");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        tvFullName.setText(user.getFullName() != null ? user.getFullName() : "");
        tvPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        tvBalance.setText(FormatUtils.formatPrice(user.getBalance()));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile(); // Reload when fragment resumes
    }
}
