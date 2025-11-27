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
            Intent intent = new Intent(this, AddCommentActivity.class);
            intent.putExtra("templateId", templateId);
            startActivity(intent);
        });

        loadReviews();
    }

    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter(this, reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
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
                        // Only show approved reviews
                        for (Review review : apiResponse.getData()) {
                            if (review.isApproved()) {
                                reviewList.add(review);
                            }
                        }
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
