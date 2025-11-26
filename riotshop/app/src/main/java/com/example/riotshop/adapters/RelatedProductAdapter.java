package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.riotshop.R;
import com.example.riotshop.models.Account;

import java.text.DecimalFormat;
import java.util.List;

public class RelatedProductAdapter extends RecyclerView.Adapter<RelatedProductAdapter.RelatedProductViewHolder> {

    private final Context context;
    private final List<Account> productList;
    private final OnRelatedProductClickListener listener;

    public interface OnRelatedProductClickListener {
        void onRelatedProductClick(Account product);
    }

    public RelatedProductAdapter(Context context, List<Account> productList, OnRelatedProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RelatedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_related_product, parent, false);
        return new RelatedProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedProductViewHolder holder, int position) {
        Account product = productList.get(position);

        holder.tvProductName.setText(product.getName());

        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvProductPrice.setText(formatter.format(product.getPrice()) + " VNĐ");

        Glide.with(context)
            .load(product.getImageResId())
            .placeholder(R.drawable.bg_category_red)
            .into(holder.ivProductImage);

        holder.itemView.setOnClickListener(v -> listener.onRelatedProductClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class RelatedProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice;

        public RelatedProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_related_product_image);
            tvProductName = itemView.findViewById(R.id.tv_related_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_related_product_price);
        }
    }
}
