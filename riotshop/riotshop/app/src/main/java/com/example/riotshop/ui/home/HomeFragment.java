package com.example.riotshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.adapters.ProductAdapter;
import com.example.riotshop.models.Product;
import com.example.riotshop.ui.product.ProductDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ProductAdapter.OnItemClickListener {

    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvProducts = view.findViewById(R.id.rv_products_fragment);
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        createMockProductList();
        productAdapter = new ProductAdapter(getContext(), productList);
        productAdapter.setOnItemClickListener(this);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(productAdapter);
    }

    private void createMockProductList() {
        productList = new ArrayList<>();
        String desc_lmht = "- Hơn 500 trang phục, bao gồm nhiều trang phục Tối Thượng và Thần Thoại.\n- Rank Bạch Kim IV, thông tin trắng, có thể thay đổi email và mật khẩu.";
        productList.add(new Product("Acc LMHT Full Skin Hiếm", "1.500.000 VNĐ", R.drawable.placeholder_account, desc_lmht));

        String desc_valorant = "- Rank Bất Tử, sở hữu nhiều skin vũ khí đắt giá từ các bundle mới nhất.\n- Thông tin đầy đủ, sẵn sàng cho việc thi đấu chuyên nghiệp.";
        productList.add(new Product("Acc Valorant Rank Bất Tử", "1.100.000 VNĐ", R.drawable.placeholder_account, desc_valorant));

        String desc_tft = "- Sở hữu tất cả các Linh Thú và Sân Đấu hiếm.\n- Rank Cao Thủ, thông tin trắng, phù hợp cho việc cày rank và stream.";
        productList.add(new Product("Acc TFT Full Tướng 3 Sao", "700.000 VNĐ", R.drawable.placeholder_account, desc_tft));
    }

    @Override
    public void onItemClick(Product product) {
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }
}
