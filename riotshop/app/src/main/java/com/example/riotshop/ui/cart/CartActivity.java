package com.example.riotshop.ui.cart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.adapters.CartAdapter;
import com.example.riotshop.models.Account; // Cần thiết để tạo dữ liệu mock
import com.example.riotshop.models.CartItem;
import com.example.riotshop.ui.other.CheckoutActivity;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity
        implements CartAdapter.CartActionListener { // 🔑 THỰC THI INTERFACE

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;

    private TextView tvTotalPrice, tvCartStatus;
    private Button btnCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart); // 🔑 Cần layout activity_cart.xml

        // 1. Ánh xạ View
        rvCartItems = findViewById(R.id.rv_cart_items);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnCheckout = findViewById(R.id.btn_checkout);
        tvCartStatus = findViewById(R.id.tv_cart_status); // Dùng để hiển thị "Giỏ hàng trống"

        // 2. Setup RecyclerView
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItemList, this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        // 3. Tải dữ liệu và cập nhật tổng tiền
        loadCartData();

        // 4. Xử lý nút Thanh toán
        btnCheckout.setOnClickListener(v -> {
            if (!cartItemList.isEmpty()) {
                startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
            } else {
                Toast.makeText(this, "Giỏ hàng trống, không thể thanh toán.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- LOGIC GIỎ HÀNG ---

    private void loadCartData() {
        // 🚨 LOGIC THỰC TẾ: Lấy dữ liệu CartItem từ Database (thường là từ Firebase/SQLite)

        // --- MOCK DATA TẠM THỜI ---
        Account accVip = new Account("acc001", "Acc Kim Cương Full Tướng", 4500000, "VIP", R.drawable.ic_launcher_background, 4.7f, "Kim Cương I", 550, 160);
        Account accSmurf = new Account("acc002", "Acc Bạc 1 Smurf", 800000, "Smurf", R.drawable.ic_launcher_foreground, 4.2f, "Bạc I", 150, 80);

        // Thêm 1 acc Vip và 2 acc Smurf (dù thường là 1, ta giả định cho logic quantity)
        cartItemList.add(new CartItem("c001", accVip.getId(), "u001", accVip, 1));
        cartItemList.add(new CartItem("c002", accSmurf.getId(), "u001", accSmurf, 2));
        // --- KẾT THÚC MOCK DATA ---

        cartAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        if (cartItemList.isEmpty()) {
            tvCartStatus.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            btnCheckout.setEnabled(false);
        } else {
            tvCartStatus.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(true);
        }

        long total = calculateTotal();
        DecimalFormat formatter = new DecimalFormat("#,###");
        String totalString = formatter.format(total);
        tvTotalPrice.setText(getString(R.string.price_format, totalString));
    }

    private long calculateTotal() {
        long total = 0;
        for (CartItem item : cartItemList) {
            item.calculateTotalItemPrice(); // Đảm bảo giá được tính lại
            total += item.getTotalItemPrice();
        }
        return total;
    }

    // --- TRIỂN KHAI PHƯƠNG THỨC TỪ INTERFACE CartAdapter.CartActionListener ---

    @Override
    public void onQuantityChange(CartItem item, int newQuantity) {
        // 🚨 LOGIC BACKEND: Cập nhật quantity của item này trong Database

        item.setQuantity(newQuantity); // Cập nhật local
        item.calculateTotalItemPrice(); // Tính lại giá
        cartAdapter.notifyDataSetChanged(); // Cập nhật giao diện
        updateUI();
        Toast.makeText(this, getString(R.string.quantity_update_toast, newQuantity), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveItem(CartItem item) {
        // 🚨 LOGIC BACKEND: Xóa item này khỏi Database

        int position = cartItemList.indexOf(item);
        if (position != -1) {
            cartItemList.remove(position);
            cartAdapter.notifyItemRemoved(position);
            updateUI();
            Toast.makeText(this, getString(R.string.item_removed_toast, item.getAccount().getName()), Toast.LENGTH_SHORT).show();
        }
    }
}