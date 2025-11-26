package com.example.riotshop.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.models.Order;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnCancelOrderListener listener;

    public interface OnCancelOrderListener {
        void onCancelOrder(Order order);
    }

    public OrderHistoryAdapter(Context context, List<Order> orderList, OnCancelOrderListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new OrderHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHistoryViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Mã đơn: " + order.getOrderId());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvOrderDate.setText("Ngày đặt: " + sdf.format(new Date(order.getOrderDate())));

        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvTotalPrice.setText("Tổng cộng: " + formatter.format(order.getTotalPrice()) + " VNĐ");

        holder.tvStatus.setText(order.getStatus());
        updateStatusView(holder.tvStatus, order.getStatus());

        // Setup nested RecyclerView for order items
        OrderItemAdapter itemAdapter = new OrderItemAdapter(context, order.getItems());
        holder.rvOrderItems.setLayoutManager(new LinearLayoutManager(context));
        holder.rvOrderItems.setAdapter(itemAdapter);

        // Handle Cancel Button
        if ("pending".equalsIgnoreCase(order.getStatus())) {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> listener.onCancelOrder(order));
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }
    }

    private void updateStatusView(TextView tvStatus, String status) {
        switch (status.toLowerCase()) {
            case "completed":
                tvStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "cancelled":
                tvStatus.setBackgroundColor(Color.parseColor("#F44336")); // Red
                break;
            default: // Pending
                tvStatus.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvStatus, tvTotalPrice;
        RecyclerView rvOrderItems;
        Button btnCancel;

        public OrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_history_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_history_order_date);
            tvStatus = itemView.findViewById(R.id.tv_history_order_status);
            tvTotalPrice = itemView.findViewById(R.id.tv_history_total_price);
            rvOrderItems = itemView.findViewById(R.id.rv_order_items);
            btnCancel = itemView.findViewById(R.id.btn_cancel_order_history);
        }
    }
}
