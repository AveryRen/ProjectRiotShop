package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.riotshop.R;
import com.example.riotshop.models.CartItem;
import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> cartItemList;
    private final CartActionListener actionListener;

    // Interface để xử lý các hành động (Tăng/Giảm/Xóa)
    public interface CartActionListener {
        void onQuantityChange(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItemList, CartActionListener actionListener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false); // 🔑 Cần layout item_cart.xml
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        // Hiển thị thông tin Account từ CartItem
        holder.tvAccountName.setText(item.getAccount().getName());
        holder.ivAccountImage.setImageResource(item.getAccount().getImageResId());

        // Giá tiền
        DecimalFormat formatter = new DecimalFormat("#,###");
        String priceString = formatter.format(item.getTotalItemPrice());
        holder.tvItemPrice.setText(context.getString(R.string.price_format, priceString));

        // Số lượng (thường là 1 trong ứng dụng bán Acc)
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Xử lý sự kiện Xóa
        holder.btnRemove.setOnClickListener(v -> {
            actionListener.onRemoveItem(item);
        });

        // Xử lý sự kiện Tăng số lượng (nếu bạn cho phép mua nhiều Acc giống nhau)
        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            actionListener.onQuantityChange(item, newQuantity);
        });

        // Xử lý sự kiện Giảm số lượng
        holder.btnDecrease.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 1) { // Chỉ giảm nếu > 1
                int newQuantity = currentQuantity - 1;
                actionListener.onQuantityChange(item, newQuantity);
            } else {
                // Có thể gọi actionListener.onRemoveItem(item) nếu giảm xuống 0
                Toast.makeText(context, R.string.use_remove_button_toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAccountImage;
        TextView tvAccountName, tvItemPrice, tvQuantity;
        ImageButton btnDecrease, btnIncrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAccountImage = itemView.findViewById(R.id.iv_cart_image);
            tvAccountName = itemView.findViewById(R.id.tv_cart_name);
            tvItemPrice = itemView.findViewById(R.id.tv_cart_price);
            tvQuantity = itemView.findViewById(R.id.tv_cart_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease_quantity);
            btnIncrease = itemView.findViewById(R.id.btn_increase_quantity);
            btnRemove = itemView.findViewById(R.id.btn_remove_cart_item);
        }
    }
}