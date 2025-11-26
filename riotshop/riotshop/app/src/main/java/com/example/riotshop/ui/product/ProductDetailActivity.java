package com.example.riotshop.ui.product;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.managers.CartManager;
import com.example.riotshop.models.Product;

import java.util.Objects;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvProductName, tvProductPrice, tvProductDescription;
    private Button btnBuyNow, btnAddToCart;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Ánh xạ Views
        ivProductImage = findViewById(R.id.iv_product_image_detail);
        tvProductName = findViewById(R.id.tv_product_name_detail);
        tvProductPrice = findViewById(R.id.tv_product_price_detail);
        tvProductDescription = findViewById(R.id.tv_product_description_detail);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        toolbar = findViewById(R.id.toolbar_product_detail);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Lấy dữ liệu sản phẩm
        Product product = getIntent().getParcelableExtra("product");

        // Hiển thị dữ liệu
        if (product != null) {
            toolbar.setTitle(product.getName());
            ivProductImage.setImageResource(product.getImage());
            tvProductName.setText(product.getName());
            tvProductPrice.setText(product.getPrice());
            tvProductDescription.setText(product.getDescription());
        }

        // Xử lý sự kiện click
        btnBuyNow.setOnClickListener(v -> Toast.makeText(ProductDetailActivity.this, "Chức năng Mua ngay đang được phát triển", Toast.LENGTH_SHORT).show());

        btnAddToCart.setOnClickListener(v -> {
            if (product != null) {
                CartManager.getInstance().addProduct(product);
                Toast.makeText(ProductDetailActivity.this, "Đã thêm '" + product.getName() + "' vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Xử lý nút back trên Toolbar
        onBackPressed();
        return true;
    }
}
