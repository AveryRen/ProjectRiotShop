package com.example.riotshop.ui.comment;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.riotshop.R;
import com.example.riotshop.data.DataSource;
import com.example.riotshop.models.User; // Assuming you have a way to get current user
import com.example.riotshop.utils.SharedPrefManager; // Assuming you use this for user session

public class AddCommentActivity extends AppCompatActivity {

    public static final String EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID";

    private RatingBar rbRating;
    private EditText etCommentText;
    private Button btnSubmit;

    private DataSource dataSource;
    private String accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        dataSource = new DataSource(this);
        dataSource.open();

        accountId = getIntent().getStringExtra(EXTRA_ACCOUNT_ID);
        if (accountId == null || accountId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có ID sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rbRating = findViewById(R.id.rb_add_comment_rating);
        etCommentText = findViewById(R.id.et_add_comment_text);
        btnSubmit = findViewById(R.id.btn_submit_comment);

        btnSubmit.setOnClickListener(v -> submitComment());
    }

    private void submitComment() {
        // You need a way to get the current logged-in user ID and Name
        // This is a placeholder. You should get this from your session manager (e.g., SharedPrefManager)
        String currentUserId = "1"; // Placeholder User ID
        String currentUserName = "Test User"; // Placeholder User Name

        float rating = rbRating.getRating();
        String text = etCommentText.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (text.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập bình luận của bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dataSource.addComment(accountId, currentUserId, currentUserName, rating, text);

        if (result != -1) {
            Toast.makeText(this, "Đã gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Gửi đánh giá thất bại.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }
}
