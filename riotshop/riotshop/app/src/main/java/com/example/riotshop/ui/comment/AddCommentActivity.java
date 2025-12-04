package com.example.riotshop.ui.comment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.CreateReviewRequest;
import com.example.riotshop.models.Review;
import com.example.riotshop.models.UpdateReviewRequest;
import com.example.riotshop.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCommentActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;
    private int templateId;
    private int reviewId = 0; // 0 means new review, > 0 means edit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        SharedPrefManager.getInstance(this);

        templateId = getIntent().getIntExtra("templateId", 0);
        reviewId = getIntent().getIntExtra("reviewId", 0);
        if (templateId == 0) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        toolbar = findViewById(R.id.toolbar_add_comment);
        ratingBar = findViewById(R.id.rating_bar);
        etComment = findViewById(R.id.et_comment);
        btnSubmit = findViewById(R.id.btn_submit_review);
        
        // If editing, load existing review data
        if (reviewId > 0) {
            int rating = getIntent().getIntExtra("rating", 0);
            String comment = getIntent().getStringExtra("comment");
            if (rating > 0) {
                ratingBar.setRating(rating);
            }
            if (comment != null) {
                // Chỉ setText khi EditText không có focus (không đang gõ)
                if (!etComment.hasFocus()) {
                    etComment.setText(comment);
                }
            }
            getSupportActionBar().setTitle("Sửa đánh giá");
            btnSubmit.setText("Cập nhật");
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thêm đánh giá");

        // Cấu hình EditText để hỗ trợ tiếng Việt
        if (etComment instanceof com.example.riotshop.widgets.VietnameseEditText) {
            com.example.riotshop.utils.EditTextUtils.configureForVietnamese(etComment);
        }

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        
        if (reviewId > 0) {
            // Update existing review
            UpdateReviewRequest updateRequest = new UpdateReviewRequest(rating, comment);
            Call<ApiResponse<Review>> call = apiService.updateReview("Bearer " + token, reviewId, updateRequest);
            handleReviewResponse(call);
        } else {
            // Create new review
            CreateReviewRequest request = new CreateReviewRequest(templateId, rating, comment);
            Call<ApiResponse<Review>> call = apiService.createReview("Bearer " + token, request);
            handleReviewResponse(call);
        }
    }
    
    private void handleReviewResponse(Call<ApiResponse<Review>> call) {

        call.enqueue(new Callback<ApiResponse<Review>>() {
            @Override
            public void onResponse(Call<ApiResponse<Review>> call, Response<ApiResponse<Review>> response) {
                btnSubmit.setEnabled(true);
                
                Log.d("AddComment", "Response code: " + response.code());
                Log.d("AddComment", "Response isSuccessful: " + response.isSuccessful());
                
                // Check if response body exists (even for error responses)
                if (response.body() != null) {
                    ApiResponse<Review> apiResponse = response.body();
                    Log.d("AddComment", "Response body success: " + apiResponse.isSuccess());
                    Log.d("AddComment", "Response body message: " + apiResponse.getMessage());
                    
                    if (apiResponse.isSuccess()) {
                        // Success case
                        String message = reviewId > 0 ? "Đã cập nhật đánh giá" : "Đã gửi đánh giá";
                        Toast.makeText(AddCommentActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                        return;
                    } else {
                        // Error case with message
                        String errorMsg = apiResponse.getMessage();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = reviewId > 0 ? "Lỗi khi cập nhật đánh giá" : "Lỗi khi gửi đánh giá";
                        }
                        Log.e("AddComment", "Error: " + errorMsg);
                        Toast.makeText(AddCommentActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                
                // Handle case when response body is null
                Log.w("AddComment", "Response body is null");
                if (response.isSuccessful()) {
                    String message = reviewId > 0 ? "Đã cập nhật đánh giá" : "Đã gửi đánh giá";
                    Toast.makeText(AddCommentActivity.this, message, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMsg = reviewId > 0 ? "Lỗi khi cập nhật đánh giá" : "Lỗi khi gửi đánh giá";
                    if (response.code() == 400) {
                        errorMsg = "Dữ liệu không hợp lệ";
                    } else if (response.code() == 401) {
                        errorMsg = "Vui lòng đăng nhập lại";
                    } else if (response.code() == 403) {
                        errorMsg = "Bạn không có quyền thực hiện thao tác này";
                    }
                    Log.e("AddComment", "HTTP Error " + response.code() + ": " + errorMsg);
                    Toast.makeText(AddCommentActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Review>> call, Throwable t) {
                btnSubmit.setEnabled(true);
                Toast.makeText(AddCommentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
