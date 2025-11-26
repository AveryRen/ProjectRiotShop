package com.example.riotshop.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.riotshop.R;
import com.example.riotshop.models.Order;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OrderActionListener listener;

    public interface OrderActionListener {
        void onConfirmOrder(Order order);
        void onCancelOrder(Order order);
    }

    public AdminOrderAdapter(Context context, List<Order> orderList, OrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText(order.getOrderId());
        holder.tvOrderUser.setText("Người mua: " + order.getUserName());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvOrderDate.setText("Ngày: " + sdf.format(new Date(order.getOrderDate())));

        DecimalFormat formatter = new DecimalFormat("#,###");
        holder.tvOrderTotal.setText("Tổng tiền: " + formatter.format(order.getTotalPrice()) + " VNĐ");

        holder.tvOrderStatus.setText(order.getStatus());
        updateStatusView(holder.tvOrderStatus, order.getStatus());

        holder.btnConfirm.setOnClickListener(v -> listener.onConfirmOrder(order));
        holder.btnCancel.setOnClickListener(v -> listener.onCancelOrder(order));
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

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderUser, tvOrderDate, tvOrderTotal, tvOrderStatus;
        Button btnConfirm, btnCancel;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderUser = itemView.findViewById(R.id.tv_order_user);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            btnConfirm = itemView.findViewById(R.id.btn_confirm_order);
            btnCancel = itemView.findViewById(R.id.btn_cancel_order);
        }
    }
}
