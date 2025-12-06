package com.example.riotshop.ui.comment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.riotshop.R;
import com.example.riotshop.adapters.ReviewAdapter;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.Review;
import com.example.riotshop.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private TextView tvEmptyReviews;
    private Button btnAddReview;
    private int templateId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        templateId = getIntent().getIntExtra("templateId", 0);
        if (templateId == 0) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar_comment);
        rvReviews = findViewById(R.id.rv_reviews);
        tvEmptyReviews = findViewById(R.id.tv_empty_reviews);
        btnAddReview = findViewById(R.id.btn_add_review);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Đánh giá");

        reviewList = new ArrayList<>();
        setupRecyclerView();

        btnAddReview.setOnClickListener(v -> {
            String token = SharedPrefManager.getInstance(this).getToken();
            if (token == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để thêm đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, AddCommentActivity.class);
            intent.putExtra("templateId", templateId);
            startActivityForResult(intent, 100);
        });

        loadReviews();
    }

    private void setupRecyclerView() {
        int currentUserId = SharedPrefManager.getInstance(this).getUserId();
        reviewAdapter = new ReviewAdapter(this, reviewList, currentUserId);
        reviewAdapter.setOnReviewActionListener(new ReviewAdapter.OnReviewActionListener() {
            @Override
            public void onEditReview(Review review) {
                Intent intent = new Intent(CommentActivity.this, AddCommentActivity.class);
                intent.putExtra("templateId", templateId);
                intent.putExtra("reviewId", review.getReviewId());
                intent.putExtra("rating", review.getRating());
                intent.putExtra("comment", review.getComment());
                startActivityForResult(intent, 100);
            }

            @Override
            public void onDeleteReview(Review review) {
                new android.app.AlertDialog.Builder(CommentActivity.this)
                    .setTitle("Xóa đánh giá")
                    .setMessage("Bạn có chắc chắn muốn xóa đánh giá này?")
                    .setPositiveButton("Xóa", (dialog, which) -> deleteReview(review.getReviewId()))
                    .setNegativeButton("Hủy", null)
                    .show();
            }
        });
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
    }
    
    private void deleteReview(int reviewId) {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<Object>> call = apiService.deleteReview("Bearer " + token, reviewId);

        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CommentActivity.this, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                    loadReviews();
                } else {
                    String errorMsg = "Lỗi khi xóa đánh giá";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(CommentActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(CommentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadReviews();
        }
    }

    private void loadReviews() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<Review>>> call = apiService.getReviewsByTemplate(templateId);

        call.enqueue(new Callback<ApiResponse<List<Review>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Review>>> call, Response<ApiResponse<List<Review>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Review>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                        reviewList.clear();
                        reviewList.addAll(apiResponse.getData()); // Show all reviews (auto approved)
                        reviewAdapter.notifyDataSetChanged();
                        showReviews();
                    } else {
                        showEmptyReviews();
                    }
                } else {
                    showEmptyReviews();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Review>>> call, Throwable t) {
                Toast.makeText(CommentActivity.this, "Lỗi khi tải đánh giá: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyReviews();
            }
        });
    }

    private void showEmptyReviews() {
        tvEmptyReviews.setVisibility(View.VISIBLE);
        rvReviews.setVisibility(View.GONE);
    }

    private void showReviews() {
        tvEmptyReviews.setVisibility(View.GONE);
        rvReviews.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReviews(); // Reload when activity resumes
    }
}
