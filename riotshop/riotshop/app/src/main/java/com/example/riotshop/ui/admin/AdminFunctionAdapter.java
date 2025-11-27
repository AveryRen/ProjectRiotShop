package com.example.riotshop.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;

import java.util.List;

public class AdminFunctionAdapter extends RecyclerView.Adapter<AdminFunctionAdapter.FunctionViewHolder> {

    private List<AdminDashboardActivity.AdminFunction> functions;

    public AdminFunctionAdapter(List<AdminDashboardActivity.AdminFunction> functions) {
        this.functions = functions;
    }

    @NonNull
    @Override
    public FunctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_function, parent, false);
        return new FunctionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FunctionViewHolder holder, int position) {
        AdminDashboardActivity.AdminFunction function = functions.get(position);
        holder.tvTitle.setText(function.getTitle());
        holder.ivIcon.setImageResource(function.getIconRes());
        
        holder.itemView.setOnClickListener(v -> {
            if (function.getOnClick() != null) {
                function.getOnClick().run();
            }
        });
    }

    @Override
    public int getItemCount() {
        return functions != null ? functions.size() : 0;
    }

    static class FunctionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView ivIcon;

        FunctionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_admin_function_title);
            ivIcon = itemView.findViewById(R.id.iv_admin_function_icon);
        }
    }
}

