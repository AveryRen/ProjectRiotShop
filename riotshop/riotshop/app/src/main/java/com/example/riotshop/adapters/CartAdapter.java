package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.models.Product;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<Product> cartItems;
    private OnItemRemoveListener removeListener;

    // Interface for remove click event
    public interface OnItemRemoveListener {
        void onItemRemove(Product product);
    }

    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        this.removeListener = listener;
    }

    public CartAdapter(Context context, List<Product> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_layout, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product product = cartItems.get(position);

        holder.tvCartItemName.setText(product.getName());
        holder.tvCartItemPrice.setText(product.getPrice());
        holder.ivCartItemImage.setImageResource(product.getImage());
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCartItemImage;
        TextView tvCartItemName, tvCartItemPrice;
        ImageButton btnRemoveFromCart;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCartItemImage = itemView.findViewById(R.id.iv_cart_item_image);
            tvCartItemName = itemView.findViewById(R.id.tv_cart_item_name);
            tvCartItemPrice = itemView.findViewById(R.id.tv_cart_item_price);
            btnRemoveFromCart = itemView.findViewById(R.id.btn_remove_from_cart);

            btnRemoveFromCart.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (removeListener != null && position != RecyclerView.NO_POSITION) {
                    removeListener.onItemRemove(cartItems.get(position));
                }
            });
        }
    }
}
