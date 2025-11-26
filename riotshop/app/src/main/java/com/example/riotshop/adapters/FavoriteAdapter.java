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

import com.bumptech.glide.Glide;
import com.example.riotshop.R;
import com.example.riotshop.models.Account;

import java.text.DecimalFormat;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private final Context context;
    private final List<Account> favoriteList;
    private final OnRemoveFavoriteListener listener;

    public interface OnRemoveFavoriteListener {
        void onRemoveFavorite(Account account);
    }

    public FavoriteAdapter(Context context, List<Account> favoriteList, OnRemoveFavoriteListener listener) {
        this.context = context;
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Account account = favoriteList.get(position);

        holder.tvFavName.setText(account.getName());

        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvFavPrice.setText(formatter.format(account.getPrice()) + " VNĐ");

        Glide.with(context)
            .load(account.getImageResId()) // Assuming you have image resources
            .placeholder(R.drawable.bg_category_red)
            .into(holder.ivFavImage);

        holder.btnRemoveFavorite.setOnClickListener(v -> listener.onRemoveFavorite(account));
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFavImage;
        TextView tvFavName, tvFavPrice;
        ImageButton btnRemoveFavorite;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFavImage = itemView.findViewById(R.id.iv_fav_image);
            tvFavName = itemView.findViewById(R.id.tv_fav_name);
            tvFavPrice = itemView.findViewById(R.id.tv_fav_price);
            btnRemoveFavorite = itemView.findViewById(R.id.btn_remove_favorite);
        }
    }
}
