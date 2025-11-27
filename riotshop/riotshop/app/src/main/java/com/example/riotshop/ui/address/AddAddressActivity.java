package com.example.riotshop.ui.address;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.CreateAddressRequest;
import com.example.riotshop.models.UserAddress;
import com.example.riotshop.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAddressActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etFullName, etPhone, etAddressLine, etCity, etDistrict, etWard;
    private CheckBox cbIsDefault;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        SharedPrefManager.getInstance(this);

        toolbar = findViewById(R.id.toolbar_add_address);
        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);
        etAddressLine = findViewById(R.id.et_address_line);
        etCity = findViewById(R.id.et_city);
        etDistrict = findViewById(R.id.et_district);
        etWard = findViewById(R.id.et_ward);
        cbIsDefault = findViewById(R.id.cb_is_default);
        btnSave = findViewById(R.id.btn_save_address);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thêm địa chỉ");

        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String addressLine = etAddressLine.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String ward = etWard.getText().toString().trim();
        boolean isDefault = cbIsDefault.isChecked();

        if (fullName.isEmpty() || phone.isEmpty() || addressLine.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        CreateAddressRequest request = new CreateAddressRequest(fullName, phone, addressLine, city, district, ward, isDefault);
        Call<ApiResponse<UserAddress>> call = apiService.createAddress("Bearer " + token, request);

        call.enqueue(new Callback<ApiResponse<UserAddress>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserAddress>> call, Response<ApiResponse<UserAddress>> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddAddressActivity.this, "Thêm địa chỉ thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddAddressActivity.this, "Lỗi khi thêm địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserAddress>> call, Throwable t) {
                btnSave.setEnabled(true);
                Toast.makeText(AddAddressActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
