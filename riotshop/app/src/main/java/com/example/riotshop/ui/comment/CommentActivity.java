package com.example.riotshop.ui.comment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.riotshop.R;
import com.example.riotshop.adapters.CommentAdapter;
import com.example.riotshop.data.DataSource;
import com.example.riotshop.models.Comment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    public static final String EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID";

    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private TextView tvNoComments;
    private FloatingActionButton btnAddComment;

    private DataSource dataSource;
    private String accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        accountId = getIntent().getStringExtra(EXTRA_ACCOUNT_ID);
        if (accountId == null || accountId.isEmpty()) {
            finish();
            return;
        }

        dataSource = new DataSource(this);
        dataSource.open();

        rvComments = findViewById(R.id.rv_comments);
        tvNoComments = findViewById(R.id.tv_no_comments);
        btnAddComment = findViewById(R.id.btn_add_comment);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        btnAddComment.setOnClickListener(v -> {
            Intent intent = new Intent(CommentActivity.this, AddCommentActivity.class);
            intent.putExtra(AddCommentActivity.EXTRA_ACCOUNT_ID, accountId);
            startActivity(intent);
        });

        loadComments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload comments when returning to the activity, in case a new one was added
        loadComments();
    }

    private void loadComments() {
        commentList.clear();
        commentList.addAll(dataSource.getCommentsForAccount(accountId));
        commentAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        if (commentList.isEmpty()) {
            tvNoComments.setVisibility(View.VISIBLE);
            rvComments.setVisibility(View.GONE);
        } else {
            tvNoComments.setVisibility(View.GONE);
            rvComments.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }
}
