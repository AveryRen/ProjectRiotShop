package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.models.UserResponse;
import com.example.riotshop.utils.FormatUtils;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private Context context;
    private List<UserResponse> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserResponse user);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    public AdminUserAdapter(Context context, List<UserResponse> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserResponse user = userList.get(position);
        if (user == null) return;

        holder.tvUsername.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        holder.tvFullName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
        holder.tvBalance.setText(FormatUtils.formatPrice(user.getBalance()));
        holder.tvIsAdmin.setText(user.isAdmin() ? "Admin" : "User");
        holder.tvIsAdmin.setTextColor(user.isAdmin() ? 
            context.getResources().getColor(R.color.riot_gold_accent) : 
            context.getResources().getColor(R.color.riot_text_light));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvFullName, tvBalance, tvIsAdmin;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_user_username);
            tvEmail = itemView.findViewById(R.id.tv_user_email);
            tvFullName = itemView.findViewById(R.id.tv_user_fullname);
            tvBalance = itemView.findViewById(R.id.tv_user_balance);
            tvIsAdmin = itemView.findViewById(R.id.tv_user_is_admin);
        }
    }
}

