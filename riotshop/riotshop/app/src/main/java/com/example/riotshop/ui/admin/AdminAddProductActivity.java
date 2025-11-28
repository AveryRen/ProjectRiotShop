package com.example.riotshop.ui.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.CreateProductRequest;
import com.example.riotshop.models.GameType;
import com.example.riotshop.utils.ImageResizer;
import com.example.riotshop.utils.SharedPrefManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.widget.ArrayAdapter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    
    private Spinner spinnerGame;
    private EditText etTitle, etDescription, etBasePrice, etTagRank, etTagSkins, etTagCollection;
    private CheckBox cbIsFeatured;
    private ImageView ivProductImage;
    private Button btnUploadImage, btnCreate;
    
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private List<GameType> games = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_product);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tạo sản phẩm mới");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        loadGames();
        setupListeners();
    }

    private void initViews() {
        spinnerGame = findViewById(R.id.spinner_game);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etBasePrice = findViewById(R.id.et_base_price);
        etTagRank = findViewById(R.id.et_tag_rank);
        etTagSkins = findViewById(R.id.et_tag_skins);
        etTagCollection = findViewById(R.id.et_tag_collection);
        cbIsFeatured = findViewById(R.id.cb_is_featured);
        ivProductImage = findViewById(R.id.iv_product_image);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnCreate = findViewById(R.id.btn_create);
    }

    private void loadGames() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<GameType>>> call = apiService.getGames();
        
        call.enqueue(new Callback<ApiResponse<List<GameType>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GameType>>> call, Response<ApiResponse<List<GameType>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    games = response.body().getData();
                    setupSpinner();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GameType>>> call, Throwable t) {
                Toast.makeText(AdminAddProductActivity.this, "Lỗi tải danh sách game", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner() {
        List<String> gameNames = new ArrayList<>();
        gameNames.add("Chọn game");
        for (GameType game : games) {
            gameNames.add(game.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gameNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGame.setAdapter(adapter);
    }

    private void setupListeners() {
        btnUploadImage.setOnClickListener(v -> {
            // Use ACTION_GET_CONTENT for better compatibility
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // Also allow picking from gallery
            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            
            Intent chooserIntent = Intent.createChooser(intent, "Chọn ảnh");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
            startActivityForResult(chooserIntent, PICK_IMAGE);
        });
        
        btnCreate.setOnClickListener(v -> createProduct());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivProductImage.setImageURI(selectedImageUri);
            uploadImage();
        }
    }

    private void uploadImage() {
        if (selectedImageUri == null) return;

        try {
            // Resize and compress image before upload
            File tempFile = ImageResizer.resizeAndCompressImage(this, selectedImageUri);
            if (tempFile == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi xử lý ảnh. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            String token = SharedPrefManager.getInstance(this).getToken();
            if (token == null) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use OkHttp for multipart upload
            OkHttpClient client = new OkHttpClient();
            
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);
            
            RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.getName(), requestFile)
                .build();

            Request request = new Request.Builder()
                .url(RetrofitClient.BASE_URL + "fileupload/image")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminAddProductActivity.this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    String responseBody = response.body().string();
                    android.util.Log.d("AdminAddProduct", "Upload response code: " + response.code());
                    android.util.Log.d("AdminAddProduct", "Upload response body: " + responseBody);
                    
                    if (response.isSuccessful()) {
                        // Parse JSON response to get URL
                        try {
                            com.google.gson.JsonObject jsonObject = new com.google.gson.Gson().fromJson(responseBody, com.google.gson.JsonObject.class);
                            
                            if (jsonObject.has("data")) {
                                Object dataObj = jsonObject.get("data");
                                String imageUrl = null;
                                
                                if (dataObj instanceof com.google.gson.JsonObject) {
                                    com.google.gson.JsonObject data = (com.google.gson.JsonObject) dataObj;
                                    if (data.has("url")) {
                                        imageUrl = data.get("url").getAsString();
                                    }
                                }
                                
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    uploadedImageUrl = imageUrl;
                                    android.util.Log.d("AdminAddProduct", "Image URL received: " + uploadedImageUrl);
                                    runOnUiThread(() -> {
                                        Toast.makeText(AdminAddProductActivity.this, "Upload ảnh thành công", Toast.LENGTH_SHORT).show();
                                        // Ensure ImageView is visible and has proper dimensions
                                        ivProductImage.setVisibility(android.view.View.VISIBLE);
                                        // Ensure ImageView has proper layout
                                        ivProductImage.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                ivProductImage.requestLayout();
                                                ivProductImage.invalidate();
                                            }
                                        });
                                        // Load uploaded image to preview
                                        com.example.riotshop.utils.ImageLoader.loadImage(ivProductImage, uploadedImageUrl);
                                    });
                                } else {
                                    android.util.Log.e("AdminAddProduct", "No URL found in response data");
                                    runOnUiThread(() -> {
                                        Toast.makeText(AdminAddProductActivity.this, "Không tìm thấy URL ảnh", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                android.util.Log.e("AdminAddProduct", "No data field in response");
                                runOnUiThread(() -> {
                                    Toast.makeText(AdminAddProductActivity.this, "Response không hợp lệ", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AdminAddProduct", "Error parsing response: " + e.getMessage(), e);
                            runOnUiThread(() -> {
                                Toast.makeText(AdminAddProductActivity.this, "Lỗi parse response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        android.util.Log.e("AdminAddProduct", "Upload failed: " + response.code() + " - " + responseBody);
                        runOnUiThread(() -> {
                            Toast.makeText(AdminAddProductActivity.this, "Lỗi upload ảnh: " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
            
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createProduct() {
        if (spinnerGame.getSelectedItemPosition() == 0 || games.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn game", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String basePriceStr = etBasePrice.getText().toString().trim();
        String tagRank = etTagRank.getText().toString().trim();
        String tagSkins = etTagSkins.getText().toString().trim();
        String tagCollection = etTagCollection.getText().toString().trim();
        boolean isFeatured = cbIsFeatured.isChecked();

        if (title.isEmpty() || basePriceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uploadedImageUrl == null || uploadedImageUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng upload ảnh sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        double basePrice;
        try {
            basePrice = Double.parseDouble(basePriceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedIndex = spinnerGame.getSelectedItemPosition() - 1;
        if (selectedIndex < 0 || selectedIndex >= games.size()) {
            Toast.makeText(this, "Game không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        GameType selectedGame = games.get(selectedIndex);
        int gameId = selectedGame.getGameId();

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        CreateProductRequest request = new CreateProductRequest(
            gameId, title, 
            description.isEmpty() ? null : description,
            basePrice, isFeatured,
            tagRank.isEmpty() ? null : tagRank,
            tagSkins.isEmpty() ? null : tagSkins,
            tagCollection.isEmpty() ? null : tagCollection,
            uploadedImageUrl
        );

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<com.example.riotshop.models.ProductTemplate>> call = apiService.createProduct("Bearer " + token, request);

        call.enqueue(new Callback<ApiResponse<com.example.riotshop.models.ProductTemplate>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.riotshop.models.ProductTemplate>> call, Response<ApiResponse<com.example.riotshop.models.ProductTemplate>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminAddProductActivity.this, "Tạo sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    // Navigate to account management
                    Intent intent = new Intent(AdminAddProductActivity.this, AdminAccountListActivity.class);
                    intent.putExtra("templateId", response.body().getData().getTemplateId());
                    startActivity(intent);
                    finish();
                } else {
                    String error = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(AdminAddProductActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.riotshop.models.ProductTemplate>> call, Throwable t) {
                Toast.makeText(AdminAddProductActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
