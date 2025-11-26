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
import com.example.riotshop.models.Address;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final Context context;
    private final List<Address> addressList;
    private final AddressActionListener actionListener;

    public interface AddressActionListener {
        void onEdit(Address address);
        void onDelete(Address address);
        void onSetDefault(Address address); // Cho phép đặt làm mặc định
        void onAddressSelected(Address address); // Xử lý khi chọn địa chỉ (trong màn hình Checkout)
    }

    public AddressAdapter(Context context, List<Address> addressList, AddressActionListener actionListener) {
        this.context = context;
        this.addressList = addressList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false); // 🔑 Cần layout item_address.xml
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);

        holder.tvRecipientName.setText(address.getRecipientName());
        holder.tvAddressDetail.setText(String.format("%s, %s, %s, %s",
                address.getStreetAddress(), address.getWard(), address.getDistrict(), address.getCity()));
        holder.tvPhoneNumber.setText(address.getPhoneNumber());

        // Hiển thị nhãn Mặc định
        if (address.isDefault()) {
            holder.tvDefaultTag.setVisibility(View.VISIBLE);
            holder.btnSetDefault.setVisibility(View.GONE);
        } else {
            holder.tvDefaultTag.setVisibility(View.GONE);
            holder.btnSetDefault.setVisibility(View.VISIBLE);
        }

        // Xử lý sự kiện
        holder.btnEdit.setOnClickListener(v -> actionListener.onEdit(address));
        holder.btnDelete.setOnClickListener(v -> actionListener.onDelete(address));
        holder.btnSetDefault.setOnClickListener(v -> actionListener.onSetDefault(address));

        // Xử lý click chọn toàn bộ item
        holder.itemView.setOnClickListener(v -> actionListener.onAddressSelected(address));
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipientName, tvAddressDetail, tvPhoneNumber, tvDefaultTag;
        Button btnEdit, btnDelete, btnSetDefault;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipientName = itemView.findViewById(R.id.tv_recipient_name);
            tvAddressDetail = itemView.findViewById(R.id.tv_address_detail);
            tvPhoneNumber = itemView.findViewById(R.id.tv_phone_number);
            tvDefaultTag = itemView.findViewById(R.id.tv_default_tag);
            btnEdit = itemView.findViewById(R.id.btn_edit_address);
            btnDelete = itemView.findViewById(R.id.btn_delete_address);
            btnSetDefault = itemView.findViewById(R.id.btn_set_default);
        }
    }
}