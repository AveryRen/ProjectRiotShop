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

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private final Context context;
    private final List<Account> accountList;
    private final OnAccountListener listener;

    public interface OnAccountListener {
        void onAccountClick(Account account);
        void onFavoriteClick(Account account);
    }

    public AccountAdapter(Context context, List<Account> accountList, OnAccountListener listener) {
        this.context = context;
        this.accountList = accountList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);

        holder.tvAccountName.setText(account.getName());

        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvAccountPrice.setText(formatter.format(account.getPrice()) + " VNĐ");

        Glide.with(context)
            .load(account.getImageResId()) // Make sure your Account model has this
            .placeholder(R.drawable.bg_category_red)
            .into(holder.ivAccountImage);

        holder.itemView.setOnClickListener(v -> listener.onAccountClick(account));
        holder.btnAddToFavorite.setOnClickListener(v -> listener.onFavoriteClick(account));
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAccountImage;
        TextView tvAccountName, tvAccountPrice;
        ImageButton btnAddToFavorite;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAccountImage = itemView.findViewById(R.id.iv_account_image);
            tvAccountName = itemView.findViewById(R.id.tv_account_name);
            tvAccountPrice = itemView.findViewById(R.id.tv_account_price);
            btnAddToFavorite = itemView.findViewById(R.id.btn_add_favorite);
        }
    }
}
