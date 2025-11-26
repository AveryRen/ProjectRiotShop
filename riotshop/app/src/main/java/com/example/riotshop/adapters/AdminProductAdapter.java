package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.riotshop.R;
import com.example.riotshop.models.Account;

import java.text.DecimalFormat;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

    private final Context context;
    private final List<Account> productList;
    private final OnProductAdminListener listener;

    public interface OnProductAdminListener {
        void onEditProduct(Account product);
        void onDeleteProduct(Account product);
    }

    public AdminProductAdapter(Context context, List<Account> productList, OnProductAdminListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
        return new AdminProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        Account product = productList.get(position);

        holder.tvProductName.setText(product.getName());

        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvProductPrice.setText(formatter.format(product.getPrice()) + " VNĐ");

        Glide.with(context)
            .load(product.getImageResId())
            .placeholder(R.drawable.bg_category_red)
            .into(holder.ivProductImage);

        holder.btnEdit.setOnClickListener(v -> listener.onEditProduct(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteProduct(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class AdminProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice;
        Button btnEdit, btnDelete;

        public AdminProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_admin_product_image);
            tvProductName = itemView.findViewById(R.id.tv_admin_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_admin_product_price);
            btnEdit = itemView.findViewById(R.id.btn_edit_product);
            btnDelete = itemView.findViewById(R.id.btn_delete_product);
        }
    }
}
