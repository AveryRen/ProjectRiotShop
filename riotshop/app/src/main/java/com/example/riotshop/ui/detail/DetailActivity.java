package com.example.riotshop.ui.detail;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.riotshop.R;
import com.example.riotshop.adapters.RelatedProductAdapter;
import com.example.riotshop.data.DataSource;
import com.example.riotshop.models.Account;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements RelatedProductAdapter.OnRelatedProductClickListener {

    private ImageView ivAccountImage;
    private TextView tvAccountName, tvAccountPrice, tvAccountRank, tvAccountSkins, tvAccountChampions;
    private Button btnAddToCart;
    private RecyclerView rvRelatedProducts;

    private RelatedProductAdapter relatedProductAdapter;
    private List<Account> relatedProductList;

    private DataSource dataSource;
    private String accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        accountId = getIntent().getStringExtra("ACCOUNT_ID");
        if (accountId == null || accountId.isEmpty()) {
            finish();
            return;
        }

        dataSource = new DataSource(this);
        dataSource.open();

        ivAccountImage = findViewById(R.id.iv_detail_image);
        tvAccountName = findViewById(R.id.tv_detail_name);
        tvAccountPrice = findViewById(R.id.tv_detail_price);
        tvAccountRank = findViewById(R.id.tv_detail_rank);
        tvAccountSkins = findViewById(R.id.tv_detail_skins);
        tvAccountChampions = findViewById(R.id.tv_detail_champions);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        rvRelatedProducts = findViewById(R.id.rv_related_products);

        setupRelatedProductsRecyclerView();
        loadAccountDetails();

        btnAddToCart.setOnClickListener(v -> {
            // Logic to add to cart in SQLite
            Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRelatedProductsRecyclerView() {
        relatedProductList = new ArrayList<>();
        relatedProductAdapter = new RelatedProductAdapter(this, relatedProductList, this);
        rvRelatedProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRelatedProducts.setAdapter(relatedProductAdapter);
    }

    private void loadAccountDetails() {
        Account account = dataSource.getAccountById(accountId);
        if (account != null) {
            populateAccountDetails(account);
            loadRelatedProducts(account.getCategory());
        } else {
            Toast.makeText(this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void populateAccountDetails(Account account) {
        tvAccountName.setText(account.getName());
        DecimalFormat formatter = new DecimalFormat("#,###");
        tvAccountPrice.setText(formatter.format(account.getPrice()) + " VNĐ");
        tvAccountRank.setText(account.getRank());
        tvAccountSkins.setText(String.valueOf(account.getSkinsOwned()));
        tvAccountChampions.setText(String.valueOf(account.getChampionsOwned()));

        Glide.with(this)
             .load(account.getImageResId())
             .placeholder(R.drawable.bg_category_red)
             .into(ivAccountImage);
    }

    private void loadRelatedProducts(String category) {
        relatedProductList.clear();
        // Get related products, excluding the current one
        List<Account> allInCategory = dataSource.getFilteredAccounts(category, 0, Float.MAX_VALUE);
        for (Account acc : allInCategory) {
            if (!acc.getId().equals(accountId)) {
                relatedProductList.add(acc);
            }
        }
        relatedProductAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRelatedProductClick(Account product) {
        Toast.makeText(this, "Clicked on " + product.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }
}
