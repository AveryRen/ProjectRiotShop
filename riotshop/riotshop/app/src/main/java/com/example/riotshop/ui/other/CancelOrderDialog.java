package com.example.riotshop.ui.other;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.CancelOrderRequest;
import com.example.riotshop.models.Order;
import com.example.riotshop.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CancelOrderDialog extends DialogFragment {

    private int orderId;
    private EditText etCancelReason;
    private Button btnCancel, btnConfirm;

    public CancelOrderDialog(int orderId) {
        this.orderId = orderId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_cancel_order, container, false);

        etCancelReason = view.findViewById(R.id.et_cancel_reason);
        btnCancel = view.findViewById(R.id.btn_cancel_dialog);
        btnConfirm = view.findViewById(R.id.btn_confirm_cancel);

        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            String reason = etCancelReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập lý do hủy đơn", Toast.LENGTH_SHORT).show();
                return;
            }
            cancelOrder(reason);
        });

        return view;
    }

    private void cancelOrder(String reason) {
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        if (token == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        CancelOrderRequest request = new CancelOrderRequest(reason);
        Call<ApiResponse<Order>> call = apiService.cancelOrder("Bearer " + token, orderId, request);

        call.enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                btnConfirm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "Đã hủy đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    dismiss();
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi hủy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                btnConfirm.setEnabled(true);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
