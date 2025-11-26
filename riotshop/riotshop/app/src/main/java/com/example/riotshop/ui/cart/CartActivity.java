package com.example.riotshop.ui.cart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.adapters.CartAdapter;
import com.example.riotshop.managers.CartManager;
import com.example.riotshop.models.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnItemRemoveListener {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<Product> cartItems;
    private TextView tvEmptyCart, tvTotalPrice;
    private Button btnCheckout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Ánh xạ Views
        rvCartItems = findViewById(R.id.rv_cart_items);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnCheckout = findViewById(R.id.btn_checkout);
        toolbar = findViewById(R.id.toolbar_cart);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Lấy dữ liệu và setup RecyclerView
        setupCart();

        btnCheckout.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Thanh toán đang được phát triển", Toast.LENGTH_SHORT).show();
        });
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
        findViewById(R.id.bottom_checkout_bar).setVisibility(View.GONE);
    }

    private void showCartItems() {
        tvEmptyCart.setVisibility(View.GONE);
        rvCartItems.setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_checkout_bar).setVisibility(View.VISIBLE);

        cartAdapter = new CartAdapter(this, cartItems);
        cartAdapter.setOnItemRemoveListener(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
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
        // Refresh the cart view
        cartAdapter.notifyDataSetChanged();
        updateTotalPrice();

        if (cartItems.isEmpty()) {
            showEmptyCart();
        }

        Toast.makeText(this, "Đã xóa '" + product.getName() + "' khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
