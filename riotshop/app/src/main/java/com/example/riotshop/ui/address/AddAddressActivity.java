package com.example.riotshop.ui.address;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.models.Address;

public class AddAddressActivity extends AppCompatActivity {

    private EditText etRecipientName, etPhoneNumber, etStreetAddress, etWard, etDistrict, etCity;
    private CheckBox cbSetDefault;
    private Button btnSaveAddress;

    // Đối tượng Address hiện tại (null nếu là thêm mới, có dữ liệu nếu là chỉnh sửa)
    private Address existingAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address); // 🔑 Cần layout activity_add_address.xml

        // 1. Ánh xạ View
        etRecipientName = findViewById(R.id.et_recipient_name);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etStreetAddress = findViewById(R.id.et_street_address);
        etWard = findViewById(R.id.et_ward);
        etDistrict = findViewById(R.id.et_district);
        etCity = findViewById(R.id.et_city);
        cbSetDefault = findViewById(R.id.cb_set_default);
        btnSaveAddress = findViewById(R.id.btn_save_address);

        // 2. Kiểm tra nếu là chế độ chỉnh sửa
        checkEditMode();

        // 3. Xử lý nút Lưu/Cập nhật
        btnSaveAddress.setOnClickListener(v -> saveAddress());
    }

    private void checkEditMode() {
        // Nhận đối tượng Address nếu được gửi từ AddressAdapter (khi người dùng click "Chỉnh sửa")
        existingAddress = getIntent().getParcelableExtra("ADDRESS_TO_EDIT");

        if (existingAddress != null) {
            setTitle("Chỉnh Sửa Địa Chỉ");
            btnSaveAddress.setText("CẬP NHẬT ĐỊA CHỈ");

            // Đổ dữ liệu cũ vào form
            etRecipientName.setText(existingAddress.getRecipientName());
            etPhoneNumber.setText(existingAddress.getPhoneNumber());
            etStreetAddress.setText(existingAddress.getStreetAddress());
            etWard.setText(existingAddress.getWard());
            etDistrict.setText(existingAddress.getDistrict());
            etCity.setText(existingAddress.getCity());
            cbSetDefault.setChecked(existingAddress.isDefault());
        } else {
            setTitle("Thêm Địa Chỉ Mới");
        }
    }

    private void saveAddress() {
        // 1. Lấy dữ liệu input
        String name = etRecipientName.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String street = etStreetAddress.getText().toString().trim();
        String ward = etWard.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        boolean isDefault = cbSetDefault.isChecked();

        // 2. Kiểm tra validation cơ bản
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(street) || TextUtils.isEmpty(city)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ các trường bắt buộc.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Tạo hoặc Cập nhật đối tượng Address
        if (existingAddress == null) {
            // Chế độ Thêm Mới
            String newId = "a_" + System.currentTimeMillis(); // Tạo ID tạm thời
            String userId = "current_user_id"; // 🚨 Cần lấy ID thực tế của người dùng đã đăng nhập

            Address newAddress = new Address(newId, userId, name, phone, street, ward, district, city, isDefault);

            // 🚨 LOGIC BACKEND: Thêm newAddress vào Database (Firestore/SQLite...)
            Toast.makeText(this, "Thêm địa chỉ mới thành công!", Toast.LENGTH_SHORT).show();
        } else {
            // Chế độ Chỉnh Sửa
            Address updatedAddress = new Address(
                    existingAddress.getAddressId(),
                    existingAddress.getUserId(),
                    name, phone, street, ward, district, city, isDefault
            );

            // 🚨 LOGIC BACKEND: Cập nhật updatedAddress trong Database
            Toast.makeText(this, "Cập nhật địa chỉ thành công!", Toast.LENGTH_SHORT).show();
        }

        // 4. Quay lại màn hình quản lý địa chỉ
        finish();
    }
}