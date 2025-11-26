package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.riotshop.R;
import com.example.riotshop.models.Account;
import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Account> accountList;
    private final ProductClickListener clickListener;

    // Interface để xử lý click từ HomeActivity
    public interface ProductClickListener {
        void onProductClick(Account account);
    }

    public ProductAdapter(Context context, List<Account> accountList, ProductClickListener clickListener) {
        this.context = context;
        this.accountList = accountList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Account account = accountList.get(position);

        holder.tvProductName.setText(account.getName());
        holder.ivProductImage.setImageResource(account.getImageResId());

        // Định dạng giá tiền (ví dụ: 1.500.000 VNĐ)
        DecimalFormat formatter = new DecimalFormat("#,###");
        String priceFormatted = formatter.format(account.getPrice()) + " VNĐ";
        holder.tvProductPrice.setText(priceFormatted);

        // Xử lý click chuyển đến trang chi tiết
        holder.itemView.setOnClickListener(v -> {
            clickListener.onProductClick(account);
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivFavoriteIcon;
        TextView tvProductName, tvProductPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            ivFavoriteIcon = itemView.findViewById(R.id.iv_favorite_icon);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }
}