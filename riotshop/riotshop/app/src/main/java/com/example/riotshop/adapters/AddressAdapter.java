package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.models.UserAddress;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context context;
    private List<UserAddress> addressList;
    private OnAddressClickListener listener;

    public interface OnAddressClickListener {
        void onAddressClick(UserAddress address);
    }

    public AddressAdapter(Context context, List<UserAddress> addressList, OnAddressClickListener listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        UserAddress address = addressList.get(position);
        
        holder.tvFullName.setText(address.getFullName());
        holder.tvPhone.setText(address.getPhoneNumber());
        
        StringBuilder addressText = new StringBuilder();
        if (address.getAddressLine() != null) addressText.append(address.getAddressLine());
        if (address.getWard() != null) addressText.append(", ").append(address.getWard());
        if (address.getDistrict() != null) addressText.append(", ").append(address.getDistrict());
        if (address.getCity() != null) addressText.append(", ").append(address.getCity());
        
        holder.tvAddress.setText(addressText.toString());
        
        if (address.isDefault()) {
            holder.tvDefault.setVisibility(View.VISIBLE);
        } else {
            holder.tvDefault.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressClick(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvPhone, tvAddress, tvDefault;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tv_address_full_name);
            tvPhone = itemView.findViewById(R.id.tv_address_phone);
            tvAddress = itemView.findViewById(R.id.tv_address_line);
            tvDefault = itemView.findViewById(R.id.tv_address_default);
        }
    }
}
