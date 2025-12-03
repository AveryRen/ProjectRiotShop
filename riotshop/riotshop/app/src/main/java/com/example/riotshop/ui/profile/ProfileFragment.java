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
import com.example.riotshop.ui.auth.LoginActivity;
import com.example.riotshop.ui.other.OrderHistoryFragment; // Assuming you have this
import com.example.riotshop.ui.other.PurchasedAccountsActivity;
import com.example.riotshop.utils.FormatUtils;
import com.example.riotshop.utils.SharedPrefManager;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail, tvBalance;
    private TextView btnEditProfile, btnOrderHistory, btnPurchasedAccounts;
    private Button btnLogout;
    private CircleImageView ivProfileAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Corrected IDs from the new layout
        ivProfileAvatar = view.findViewById(R.id.iv_profile_avatar);
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        tvBalance = view.findViewById(R.id.tv_balance);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnOrderHistory = view.findViewById(R.id.btn_order_history);
        btnPurchasedAccounts = view.findViewById(R.id.btn_purchased_accounts);
        btnLogout = view.findViewById(R.id.btn_logout);

        SharedPrefManager.getInstance(getContext());

        setClickListeners();
        loadUserProfile();

        return view;
    }

    private void setClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        btnOrderHistory.setOnClickListener(v -> {
            // You might want to replace the fragment instead of starting a new activity
            // For now, let's assume you have an activity or fragment for this
            Toast.makeText(getContext(), "Chức năng Lịch sử đơn hàng đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        btnPurchasedAccounts.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PurchasedAccountsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void handleLogout() {
        SharedPrefManager.getInstance(getContext()).logout();
        Toast.makeText(getContext(), "Đăng Xuất thành công", Toast.LENGTH_SHORT).show();
        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void loadUserProfile() {
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        if (token == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            // Redirect to login or handle appropriately
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
        tvProfileName.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
        tvProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        tvBalance.setText(FormatUtils.formatPrice(user.getBalance()));

        // You can use a library like Picasso or Glide to load the avatar
        // Picasso.get().load(user.getAvatarUrl()).placeholder(R.drawable.ic_profile_default).into(ivProfileAvatar);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile(); // Reload when fragment resumes
    }
}
