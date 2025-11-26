package com.example.riotshop.ui.detail;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.riotshop.R;
import com.example.riotshop.models.Account; // 🔑 DÙNG ACCOUNT
import java.text.DecimalFormat;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivAccountImage;
    private TextView tvAccountName, tvAccountPrice, tvAccountRank, tvAccountSkins, tvAccountChampions;
    private Button btnAddToCart;
    private Account currentAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // 1. Ánh xạ View
        ivAccountImage = findViewById(R.id.iv_detail_image);
        tvAccountName = findViewById(R.id.tv_detail_name);
        tvAccountPrice = findViewById(R.id.tv_detail_price);
        // ... (Ánh xạ các TextView khác) ...
        tvAccountRank = findViewById(R.id.tv_detail_rank);
        tvAccountSkins = findViewById(R.id.tv_detail_skins);
        tvAccountChampions = findViewById(R.id.tv_detail_champions);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);

        // 2. Nhận dữ liệu từ Intent
        currentAccount = getIntent().getParcelableExtra("account_detail"); // 🔑 KEY từ HomeActivity

        if (currentAccount != null) {
            // 3. Hiển thị dữ liệu
            displayAccountDetails(currentAccount);

            // 4. Xử lý nút Thêm vào Giỏ hàng
            btnAddToCart.setOnClickListener(v -> {
                handleAddToCart(currentAccount);
            });
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayAccountDetails(Account account) {
        tvAccountName.setText(account.getName());
        ivAccountImage.setImageResource(account.getImageResId());

        DecimalFormat formatter = new DecimalFormat("#,###");
        String priceFormatted = formatter.format(account.getPrice()) + " VNĐ";
        tvAccountPrice.setText(priceFormatted);

        // Hiển thị thông tin cụ thể của ACC LMHT
        tvAccountRank.setText("Bậc Rank: " + account.getRank());
        tvAccountSkins.setText("Số Trang phục: " + account.getSkinsOwned());
        tvAccountChampions.setText("Số Tướng: " + account.getChampionsOwned());
    }

    private void handleAddToCart(Account account) {
        // 🚨 CHỖ NÀY CẦN GỌI HÀM LƯU CartItem VÀO Database/SharedPref/Local

        Toast.makeText(this, "Đã thêm tài khoản " + account.getName() + " vào Giỏ hàng!", Toast.LENGTH_SHORT).show();
    }
}