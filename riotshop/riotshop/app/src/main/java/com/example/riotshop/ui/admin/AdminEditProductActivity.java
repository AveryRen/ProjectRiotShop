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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.GameType;
import com.example.riotshop.models.ProductTemplate;
import com.example.riotshop.models.UpdateProductRequest;
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

public class AdminEditProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    
    private int templateId;
    private Spinner spinnerGame;
    private EditText etTitle, etDescription, etBasePrice, etTagRank, etTagSkins, etTagCollection;
    private CheckBox cbIsFeatured;
    private ImageView ivProductImage;
    private Button btnUploadImage, btnUpdate, btnDelete;
    
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private List<GameType> games = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_product);

        templateId = getIntent().getIntExtra("templateId", -1);
        if (templateId == -1) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sửa sản phẩm");
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
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
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
                    // Load product after spinner is set up
                    loadProduct();
                } else {
                    Toast.makeText(AdminEditProductActivity.this, "Lỗi tải danh sách game", Toast.LENGTH_SHORT).show();
                    // Still try to load product even if games fail
                    loadProduct();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GameType>>> call, Throwable t) {
                Toast.makeText(AdminEditProductActivity.this, "Lỗi tải danh sách game", Toast.LENGTH_SHORT).show();
                // Still try to load product even if games fail
                loadProduct();
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

    private void loadProduct() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<ProductTemplate>> call = apiService.getProductById(templateId);
        
        call.enqueue(new Callback<ApiResponse<ProductTemplate>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductTemplate>> call, Response<ApiResponse<ProductTemplate>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ProductTemplate product = response.body().getData();
                    
                    // Load all product information
                    if (product.getTitle() != null) {
                        etTitle.setText(product.getTitle());
                    }
                    
                    if (product.getDescription() != null) {
                        etDescription.setText(product.getDescription());
                    } else {
                        etDescription.setText("");
                    }
                    
                    etBasePrice.setText(String.valueOf(product.getBasePrice()));
                    
                    if (product.getTagRank() != null) {
                        etTagRank.setText(product.getTagRank());
                    } else {
                        etTagRank.setText("");
                    }
                    
                    if (product.getTagSkins() != null) {
                        etTagSkins.setText(product.getTagSkins());
                    } else {
                        etTagSkins.setText("");
                    }
                    
                    if (product.getTagCollection() != null) {
                        etTagCollection.setText(product.getTagCollection());
                    } else {
                        etTagCollection.setText("");
                    }
                    
                    uploadedImageUrl = product.getImageUrl();
                    cbIsFeatured.setChecked(product.isFeatured());
                    
                    // Set selected game in spinner after games are loaded
                    if (!games.isEmpty()) {
                        for (int i = 0; i < games.size(); i++) {
                            if (games.get(i).getGameId() == product.getGameId()) {
                                spinnerGame.setSelection(i + 1); // +1 because first item is "Chọn game"
                                break;
                            }
                        }
                    } else {
                        // If games not loaded yet, try to set after a delay
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (!games.isEmpty()) {
                                for (int i = 0; i < games.size(); i++) {
                                    if (games.get(i).getGameId() == product.getGameId()) {
                                        spinnerGame.setSelection(i + 1);
                                        break;
                                    }
                                }
                            }
                        }, 500);
                    }
                    
                    // Load product image
                    if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                        uploadedImageUrl = product.getImageUrl();
                        // Ensure ImageView is visible
                        ivProductImage.setVisibility(android.view.View.VISIBLE);
                        // Load image using ImageLoader helper
                        com.example.riotshop.utils.ImageLoader.loadImage(ivProductImage, product.getImageUrl());
                    } else {
                        uploadedImageUrl = null;
                        ivProductImage.setVisibility(android.view.View.VISIBLE);
                    }
                    
                    // Update toolbar title with product name
                    if (getSupportActionBar() != null && product.getTitle() != null) {
                        getSupportActionBar().setTitle("Sửa sản phẩm: " + product.getTitle());
                    }
                } else {
                    Toast.makeText(AdminEditProductActivity.this, "Lỗi tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductTemplate>> call, Throwable t) {
                Toast.makeText(AdminEditProductActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
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
        
        btnUpdate.setOnClickListener(v -> updateProduct());
        btnDelete.setOnClickListener(v -> deleteProduct());
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

            OkHttpClient client = new OkHttpClient();
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
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
                        Toast.makeText(AdminEditProductActivity.this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    String responseBody = response.body().string();
                    android.util.Log.d("AdminEditProduct", "Upload response code: " + response.code());
                    android.util.Log.d("AdminEditProduct", "Upload response body: " + responseBody);
                    
                    if (response.isSuccessful()) {
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
                                    android.util.Log.d("AdminEditProduct", "Image URL received: " + uploadedImageUrl);
                                    runOnUiThread(() -> {
                                        Toast.makeText(AdminEditProductActivity.this, "Upload ảnh thành công", Toast.LENGTH_SHORT).show();
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
                                    android.util.Log.e("AdminEditProduct", "No URL found in response data");
                                    runOnUiThread(() -> {
                                        Toast.makeText(AdminEditProductActivity.this, "Không tìm thấy URL ảnh", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                android.util.Log.e("AdminEditProduct", "No data field in response");
                                runOnUiThread(() -> {
                                    Toast.makeText(AdminEditProductActivity.this, "Response không hợp lệ", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AdminEditProduct", "Error parsing response: " + e.getMessage(), e);
                            runOnUiThread(() -> {
                                Toast.makeText(AdminEditProductActivity.this, "Lỗi parse response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        android.util.Log.e("AdminEditProduct", "Upload failed: " + response.code() + " - " + responseBody);
                        runOnUiThread(() -> {
                            Toast.makeText(AdminEditProductActivity.this, "Lỗi upload ảnh: " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProduct() {
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

        // Get selected game
        int selectedIndex = spinnerGame.getSelectedItemPosition() - 1;
        if (selectedIndex < 0 || selectedIndex >= games.size()) {
            Toast.makeText(this, "Vui lòng chọn game", Toast.LENGTH_SHORT).show();
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

        UpdateProductRequest request = new UpdateProductRequest();
        request.setGameId(gameId);
        request.setTitle(title);
        request.setDescription(description.isEmpty() ? null : description);
        request.setBasePrice(basePrice);
        request.setIsFeatured(isFeatured);
        request.setTagRank(tagRank.isEmpty() ? null : tagRank);
        request.setTagSkins(tagSkins.isEmpty() ? null : tagSkins);
        request.setTagCollection(tagCollection.isEmpty() ? null : tagCollection);
        request.setImageUrl(uploadedImageUrl);

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<ProductTemplate>> call = apiService.updateProduct("Bearer " + token, templateId, request);

        call.enqueue(new Callback<ApiResponse<ProductTemplate>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductTemplate>> call, Response<ApiResponse<ProductTemplate>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AdminEditProductActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = response.body() != null ? response.body().getMessage() : response.message();
                    Toast.makeText(AdminEditProductActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductTemplate>> call, Throwable t) {
                Toast.makeText(AdminEditProductActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProduct() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa sản phẩm này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                String token = SharedPrefManager.getInstance(this).getToken();
                if (token == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                ApiService apiService = RetrofitClient.getInstance().getApiService();
                Call<ApiResponse<Object>> call = apiService.deleteProduct("Bearer " + token, templateId);

                call.enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(AdminEditProductActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String error = response.body() != null ? response.body().getMessage() : response.message();
                            Toast.makeText(AdminEditProductActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        Toast.makeText(AdminEditProductActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
}
