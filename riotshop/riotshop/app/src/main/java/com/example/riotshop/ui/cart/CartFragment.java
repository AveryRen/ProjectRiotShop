package com.example.riotshop.ui.cart;

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
import com.example.riotshop.managers.CartManager;
import com.example.riotshop.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment implements CartAdapter.OnItemRemoveListener {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<Product> cartItems;
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

        setupCart();

        btnCheckout.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng Thanh toán đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void setupCart() {
        cartItems = CartManager.getInstance().getCartItems();

        if (cartItems.isEmpty()) {
            showEmptyCart();
        } else {
            showCartItems();
        }
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
        for (Product product : cartItems) {
            try {
                String priceString = product.getPrice().replaceAll("[^\\d]", "");
                total += Double.parseDouble(priceString);
            } catch (NumberFormatException e) {
                // Bỏ qua sản phẩm có giá không hợp lệ
            }
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(String.format("Tổng cộng: %s", currencyFormat.format(total)));
    }

    @Override
    public void onItemRemove(Product product) {
        CartManager.getInstance().removeProduct(product);
        cartAdapter.notifyDataSetChanged();
        updateTotalPrice();

        if (cartItems.isEmpty()) {
            showEmptyCart();
        }

        Toast.makeText(getContext(), "Đã xóa '" + product.getName() + "' khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
    }
}
