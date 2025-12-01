package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.models.PaymentTransaction;
import com.example.riotshop.utils.FormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<PaymentTransaction> transactionList;

    public TransactionAdapter(Context context, List<PaymentTransaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        PaymentTransaction transaction = transactionList.get(position);
        if (transaction == null) return;

        // Set amount
        holder.tvAmount.setText(FormatUtils.formatPrice(transaction.getAmount()));

        // Set transaction ID
        holder.tvTransactionId.setText("ID: #" + transaction.getTransactionId());

        // Set status with appropriate color and background
        String status = transaction.getStatus();
        holder.tvStatus.setText(getStatusText(status));
        
        // Set status background and text color
        int bgResId = getStatusBackground(status);
        int textColor = getStatusTextColor(status);
        holder.tvStatus.setBackgroundResource(bgResId);
        holder.tvStatus.setTextColor(textColor);

        // Set date
        String dateStr = formatDate(transaction.getCreatedAt());
        if (transaction.getCompletedAt() != null && !transaction.getCompletedAt().isEmpty()) {
            dateStr = formatDate(transaction.getCompletedAt());
        }
        holder.tvDate.setText(dateStr);

        // Show failure reason if failed
        if ("failed".equalsIgnoreCase(status) && transaction.getFailureReason() != null && !transaction.getFailureReason().isEmpty()) {
            holder.tvFailureReason.setText("Lý do: " + transaction.getFailureReason());
            holder.tvFailureReason.setVisibility(View.VISIBLE);
        } else {
            holder.tvFailureReason.setVisibility(View.GONE);
        }
    }

    private String getStatusText(String status) {
        if (status == null) return "Không xác định";
        switch (status.toLowerCase()) {
            case "succeeded":
                return "Thành công";
            case "pending":
                return "Đang xử lý";
            case "failed":
                return "Thất bại";
            case "canceled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private int getStatusBackground(String status) {
        if (status == null) return R.drawable.bg_status_pending;
        switch (status.toLowerCase()) {
            case "succeeded":
                return R.drawable.bg_status_success;
            case "pending":
                return R.drawable.bg_status_pending;
            case "failed":
                return R.drawable.bg_status_failed;
            case "canceled":
                return R.drawable.bg_status_canceled;
            default:
                return R.drawable.bg_status_pending;
        }
    }

    private int getStatusTextColor(String status) {
        return context.getResources().getColor(R.color.riot_text_light);
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        
        try {
            // Parse ISO 8601 format from backend
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            
            if (date == null) {
                // Try with milliseconds
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                date = inputFormat.parse(dateStr);
            }
            
            if (date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            // If parsing fails, return original string
        }
        
        return dateStr;
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvStatus, tvTransactionId, tvDate, tvFailureReason;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTransactionId = itemView.findViewById(R.id.tv_transaction_id);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvFailureReason = itemView.findViewById(R.id.tv_failure_reason);
        }
    }
}

