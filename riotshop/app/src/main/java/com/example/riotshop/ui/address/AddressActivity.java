package com.example.riotshop.ui.address;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.adapters.AddressAdapter;
import com.example.riotshop.models.Address;
import java.util.ArrayList;
import java.util.List;

public class AddressActivity extends AppCompatActivity
        implements AddressAdapter.AddressActionListener { // 🔑 THỰC THI INTERFACE

    private RecyclerView rvAddressList;
    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private Button btnAddAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address); // 🔑 Cần layout activity_address.xml

        // Ánh xạ View
        rvAddressList = findViewById(R.id.rv_address_list);
        btnAddAddress = findViewById(R.id.btn_add_new_address);

        // 1. Setup RecyclerView
        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressList, this);
        rvAddressList.setLayoutManager(new LinearLayoutManager(this));
        rvAddressList.setAdapter(addressAdapter);

        // 2. Tải dữ liệu Mock/Thực tế
        loadAddressData();

        // 3. Xử lý nút Thêm địa chỉ mới
        btnAddAddress.setOnClickListener(v -> {
            startActivity(new Intent(AddressActivity.this, AddAddressActivity.class));
        });
    }

    // Phương thức tạo dữ liệu mẫu
    private void loadAddressData() {
        // --- MOCK DATA ---
        addressList.add(new Address("a001", "u001", "Nguyễn Văn A", "0901234567",
                "123 Đường Bán Acc", "Phường 1", "Quận Bình Thạnh", "TP Hồ Chí Minh", true)); // Mặc định
        addressList.add(new Address("a002", "u001", "Trần Thị B", "0987654321",
                "456 Hẻm Rank Cao", "Phường 5", "Quận 1", "TP Hồ Chí Minh", false));
        // --- KẾT THÚC MOCK DATA ---
        addressAdapter.notifyDataSetChanged();
    }

    // --- TRIỂN KHAI PHƯƠNG THỨC TỪ INTERFACE AddressAdapter.AddressActionListener ---

    @Override
    public void onEdit(Address address) {
        Toast.makeText(this, "Chỉnh sửa địa chỉ: " + address.getStreetAddress(), Toast.LENGTH_SHORT).show();
        // 🚨 CHUYỂN ĐẾN AddAddressActivity (hoặc EditAddressActivity) và truyền đối tượng Address đi
    }

    @Override
    public void onDelete(Address address) {
        Toast.makeText(this, "Xóa địa chỉ: " + address.getStreetAddress(), Toast.LENGTH_SHORT).show();
        // 🚨 Cần thêm logic xác nhận xóa và xóa khỏi danh sách/database
    }

    @Override
    public void onSetDefault(Address address) {
        Toast.makeText(this, "Đặt làm mặc định: " + address.getRecipientName(), Toast.LENGTH_SHORT).show();
        // 🚨 Cần thêm logic cập nhật trạng thái mặc định trong toàn bộ danh sách
    }

    @Override
    public void onAddressSelected(Address address) {
        // Logic này quan trọng nếu màn hình này được gọi từ CheckoutActivity
        Toast.makeText(this, "Đã chọn: " + address.getStreetAddress(), Toast.LENGTH_SHORT).show();
        // 🚨 Nếu gọi từ Checkout: cần trả về kết quả cho Activity gọi nó (dùng setResult)
    }
}