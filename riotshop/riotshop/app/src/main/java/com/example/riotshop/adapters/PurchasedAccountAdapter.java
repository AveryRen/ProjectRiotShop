package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.models.PurchasedAccount;
import com.example.riotshop.utils.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PurchasedAccountAdapter extends RecyclerView.Adapter<PurchasedAccountAdapter.AccountViewHolder> {

    private Context context;
    private List<PurchasedAccount> accountList;
    private OnAccountClickListener listener;

    public interface OnAccountClickListener {
        void onCopyUsername(PurchasedAccount account);
        void onCopyPassword(PurchasedAccount account);
        void onCopyRiotId(PurchasedAccount account);
    }

    public PurchasedAccountAdapter(Context context, List<PurchasedAccount> accountList, OnAccountClickListener listener) {
        this.context = context;
        this.accountList = accountList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_purchased_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        PurchasedAccount account = accountList.get(position);
        
        if (account.getAccountDetail() != null) {
            // Product info
            if (account.getAccountDetail().getProductTemplate() != null) {
                holder.tvProductName.setText(account.getAccountDetail().getProductTemplate().getTitle());
                // Lấy gameName từ ProductTemplate (có thể từ gameName field hoặc gameType)
                String gameName = account.getAccountDetail().getProductTemplate().getGameName();
                if (gameName == null || gameName.isEmpty()) {
                    gameName = "Game";
                }
                holder.tvGameName.setText(gameName);
            } else {
                holder.tvProductName.setText("N/A");
                holder.tvGameName.setText("");
            }
            
            // Account credentials
            holder.tvUsername.setText(account.getAccountDetail().getAccountUsername() != null ? 
                account.getAccountDetail().getAccountUsername() : "N/A");
            holder.tvPassword.setText(account.getAccountDetail().getAccountPassword() != null ? 
                "••••••••" : "N/A"); // Hide password, show dots
            holder.tvRiotId.setText(account.getAccountDetail().getRiotId() != null ? 
                account.getAccountDetail().getRiotId() : "N/A");
            
            // Order date
            if (account.getOrderDate() != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date date = sdf.parse(account.getOrderDate());
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    holder.tvOrderDate.setText("Ngày mua: " + displayFormat.format(date));
                } catch (Exception e) {
                    holder.tvOrderDate.setText("Ngày mua: " + account.getOrderDate());
                }
            }
            
            // Price
            holder.tvPrice.setText("Giá: " + FormatUtils.formatPrice(account.getTotalAmount()));
            
            // Copy buttons
            holder.btnCopyUsername.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCopyUsername(account);
                }
            });
            
            holder.btnCopyPassword.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCopyPassword(account);
                }
            });
            
            holder.btnCopyRiotId.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCopyRiotId(account);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvGameName, tvUsername, tvPassword, tvRiotId, tvOrderDate, tvPrice;
        Button btnCopyUsername, btnCopyPassword, btnCopyRiotId;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvGameName = itemView.findViewById(R.id.tv_game_name);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvPassword = itemView.findViewById(R.id.tv_password);
            tvRiotId = itemView.findViewById(R.id.tv_riot_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnCopyUsername = itemView.findViewById(R.id.btn_copy_username);
            btnCopyPassword = itemView.findViewById(R.id.btn_copy_password);
            btnCopyRiotId = itemView.findViewById(R.id.btn_copy_riot_id);
        }
    }
}

