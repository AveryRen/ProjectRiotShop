package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class AdminAccountAdapter extends RecyclerView.Adapter<AdminAccountAdapter.AccountViewHolder> {

    private Context context;
    private List<Object> accountList;
    private OnAccountClickListener listener;

    public interface OnAccountClickListener {
        void onAccountClick(Object account);
    }

    public void setOnAccountClickListener(OnAccountClickListener listener) {
        this.listener = listener;
    }

    public AdminAccountAdapter(Context context, List<Object> accountList) {
        this.context = context;
        this.accountList = accountList;
    }

    public void updateAccounts(List<Object> accounts) {
        this.accountList = accounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Object obj = accountList.get(position);
        if (obj == null) return;

        Gson gson = new Gson();
        try {
            String json = gson.toJson(obj);
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> account = gson.fromJson(json, type);

            Integer accDetailId = account.get("accDetailId") != null ? 
                ((Double) account.get("accDetailId")).intValue() : 0;
            String username = account.get("accountUsername") != null ? 
                (String) account.get("accountUsername") : "";
            String riotId = account.get("riotId") != null ? 
                (String) account.get("riotId") : "-";
            String recoveryEmail = account.get("recoveryEmail") != null ? 
                (String) account.get("recoveryEmail") : "-";
            Double originalPrice = account.get("originalPrice") != null ? 
                (Double) account.get("originalPrice") : 0.0;
            Boolean isSold = account.get("isSold") != null ? 
                (Boolean) account.get("isSold") : false;

            holder.tvId.setText("ID: " + accDetailId);
            holder.tvUsername.setText(username);
            holder.tvRiotId.setText("Riot ID: " + riotId);
            holder.tvEmail.setText("Email: " + recoveryEmail);
            holder.tvPrice.setText(String.format("%.0f đ", originalPrice));
            holder.tvStatus.setText(isSold ? "Đã bán" : "Còn hàng");
            holder.tvStatus.setTextColor(isSold ? 
                context.getResources().getColor(R.color.error) : 
                context.getResources().getColor(R.color.success));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccountClick(obj);
                }
            });
        } catch (Exception e) {
            // Skip invalid accounts
        }
    }

    @Override
    public int getItemCount() {
        return accountList != null ? accountList.size() : 0;
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvUsername, tvRiotId, tvEmail, tvPrice, tvStatus;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_account_id);
            tvUsername = itemView.findViewById(R.id.tv_account_username);
            tvRiotId = itemView.findViewById(R.id.tv_account_riot_id);
            tvEmail = itemView.findViewById(R.id.tv_account_email);
            tvPrice = itemView.findViewById(R.id.tv_account_price);
            tvStatus = itemView.findViewById(R.id.tv_account_status);
        }
    }
}

