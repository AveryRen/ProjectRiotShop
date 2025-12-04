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
import com.example.riotshop.models.CartItem;
import com.example.riotshop.utils.FormatUtils;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnItemRemoveListener removeListener;

    // Interface for remove click event
    public interface OnItemRemoveListener {
        void onItemRemove(CartItem cartItem);
    }

    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        this.removeListener = listener;
    }

    public CartAdapter(Context context, List<CartItem> cartItems) {
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
        CartItem cartItem = cartItems.get(position);
        
        if (cartItem.getProductTemplate() != null) {
            holder.tvCartItemName.setText(cartItem.getProductTemplate().getTitle());
            double totalPrice = cartItem.getProductTemplate().getBasePrice() * cartItem.getQuantity();
            holder.tvCartItemPrice.setText(FormatUtils.formatPrice(totalPrice));
            if (holder.tvQuantity != null) {
                holder.tvQuantity.setText("x" + cartItem.getQuantity());
            }
            
            // Load image from URL if available, otherwise use placeholder
            String imageUrl = cartItem.getProductTemplate().getImageUrl();
            android.util.Log.d("CartAdapter", "Cart item: " + cartItem.getProductTemplate().getTitle() + ", imageUrl: " + imageUrl);
            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.trim().isEmpty()) {
                android.util.Log.d("CartAdapter", "Loading image from URL: " + imageUrl);
                com.example.riotshop.utils.ImageLoader.loadImage(holder.ivCartItemImage, imageUrl);
            } else {
                android.util.Log.w("CartAdapter", "Image URL is null or empty for cart item: " + cartItem.getProductTemplate().getTitle());
                holder.ivCartItemImage.setImageResource(R.drawable.placeholder_account);
            }
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCartItemImage;
        TextView tvCartItemName, tvCartItemPrice, tvQuantity;
        ImageButton btnRemoveFromCart;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCartItemImage = itemView.findViewById(R.id.iv_cart_item_image);
            tvCartItemName = itemView.findViewById(R.id.tv_cart_item_name);
            tvCartItemPrice = itemView.findViewById(R.id.tv_cart_item_price);
            tvQuantity = itemView.findViewById(R.id.tv_cart_item_quantity);
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
