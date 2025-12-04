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
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnItemClickListener listener;
    private OnFavoriteClickListener favoriteListener;
    private Set<Integer> favoriteTemplateIds; // Track which products are favorited

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Product product, boolean isFavorite);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteListener = listener;
    }

    public void setFavoriteTemplateIds(Set<Integer> favoriteIds) {
        this.favoriteTemplateIds = favoriteIds;
        notifyDataSetChanged();
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product == null) return;

        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(product.getPrice());
        
        // Load image from URL if available, otherwise use placeholder resource
        String imageUrl = product.getImageUrl();
        android.util.Log.d("ProductAdapter", "Product: " + product.getName() + ", imageUrl: " + imageUrl);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            android.util.Log.d("ProductAdapter", "Loading image from URL: " + imageUrl);
            com.example.riotshop.utils.ImageLoader.loadImage(holder.ivProductImage, imageUrl);
        } else {
            android.util.Log.w("ProductAdapter", "Image URL is null or empty for product: " + product.getName());
            holder.ivProductImage.setImageResource(product.getImage());
        }
        
        // CORRECTED: Use the int resource from product.getImage()
        holder.ivProductImage.setImageResource(product.getImage());

        // Update favorite icon state
        boolean isFavorite = favoriteTemplateIds != null && favoriteTemplateIds.contains(product.getTemplateId());
        holder.btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_gold : R.drawable.ic_favorite_border_gold);

        holder.btnFavorite.setOnClickListener(v -> {
            if (favoriteListener != null) {
                favoriteListener.onFavoriteClick(product, isFavorite);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        ImageButton btnFavorite;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_title);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(productList.get(position));
                }
            });
        }
    }
}
