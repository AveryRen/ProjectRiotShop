package com.example.riotshop.ui.cart;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.adapters.CartAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.CartItem;
import com.example.riotshop.ui.other.CheckoutActivity;
import com.example.riotshop.utils.SharedPrefManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment implements CartAdapter.OnItemRemoveListener {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private TextView tvEmptyCart, tvTotalPrice;
    private Button btnCheckout;
    private View bottomBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCartItems = view.findViewById(R.id.rv_cart_items_fragment);
        tvEmptyCart = view.findViewById(R.id.tv_empty_cart_fragment);
        tvTotalPrice = view.findViewById(R.id.tv_total_price_fragment);
        btnCheckout = view.findViewById(R.id.btn_checkout_fragment);
        bottomBar = view.findViewById(R.id.bottom_checkout_bar_fragment);

        cartItems = new ArrayList<>();
        loadCart();

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), CheckoutActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadCart() {
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        if (token == null) {
            showEmptyCart();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<CartItem>>> call = apiService.getCart("Bearer " + token);
        
        call.enqueue(new Callback<ApiResponse<List<CartItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CartItem>>> call, Response<ApiResponse<List<CartItem>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<CartItem>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        cartItems.clear();
                        cartItems.addAll(apiResponse.getData());
                        if (cartItems.isEmpty()) {
                            showEmptyCart();
                        } else {
                            showCartItems();
                        }
                    } else {
                        showEmptyCart();
                    }
                } else {
                    showEmptyCart();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CartItem>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi tải giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyCart();
            }
        });
    }

    private void showEmptyCart() {
        tvEmptyCart.setVisibility(View.VISIBLE);
        rvCartItems.setVisibility(View.GONE);
        bottomBar.setVisibility(View.GONE);
    }

    private void showCartItems() {
        tvEmptyCart.setVisibility(View.GONE);
        rvCartItems.setVisibility(View.VISIBLE);
        bottomBar.setVisibility(View.VISIBLE);

        cartAdapter = new CartAdapter(getContext(), cartItems);
        cartAdapter.setOnItemRemoveListener(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(cartAdapter);

        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.getProductTemplate() != null) {
                total += item.getProductTemplate().getBasePrice() * item.getQuantity();
            }
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(String.format("Tổng cộng: %s", currencyFormat.format(total)));
    }

    @Override
    public void onItemRemove(CartItem cartItem) {
        String token = SharedPrefManager.getInstance(getContext()).getToken();
        if (token == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.removeCartItem("Bearer " + token, cartItem.getCartItemId());
        
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cartItems.remove(cartItem);
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                    
                    if (cartItems.isEmpty()) {
                        showEmptyCart();
                    }
                    
                    String productName = cartItem.getProductTemplate() != null ? 
                        cartItem.getProductTemplate().getTitle() : "Sản phẩm";
                    Toast.makeText(getContext(), "Đã xóa '" + productName + "' khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadCart(); // Reload cart when fragment resumes
    }
}
