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
import com.example.riotshop.models.CartItem;

import java.text.DecimalFormat;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private final Context context;
    private final List<CartItem> itemList;

    public OrderItemAdapter(Context context, List<CartItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        CartItem item = itemList.get(position);

        if (item.getAccount() != null) {
            holder.tvItemName.setText(item.getAccount().getName());

            DecimalFormat formatter = new DecimalFormat("#,###");
            holder.tvItemPrice.setText(formatter.format(item.getAccount().getPrice()) + " VNĐ");

            Glide.with(context)
                .load(item.getAccount().getImageResId())
                .placeholder(R.drawable.bg_category_red)
                .into(holder.ivItemImage);
        }

        holder.tvItemQuantity.setText("Số lượng: " + item.getQuantity());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvItemName, tvItemQuantity, tvItemPrice;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.iv_order_item_image);
            tvItemName = itemView.findViewById(R.id.tv_order_item_name);
            tvItemQuantity = itemView.findViewById(R.id.tv_order_item_quantity);
            tvItemPrice = itemView.findViewById(R.id.tv_order_item_price);
        }
    }
}
