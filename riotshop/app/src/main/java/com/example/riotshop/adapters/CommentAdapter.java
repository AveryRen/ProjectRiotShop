package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.riotshop.R;
import com.example.riotshop.models.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.tvUserName.setText(comment.getUserName());
        holder.tvCommentText.setText(comment.getText());
        holder.rbCommentRating.setRating(comment.getRating());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvCommentDate.setText(sdf.format(new Date(comment.getTimestamp())));

        if (comment.getUserAvatarUrl() != null && !comment.getUserAvatarUrl().isEmpty()) {
            Glide.with(context)
                .load(comment.getUserAvatarUrl())
                .placeholder(R.drawable.bg_rounded_profile_gold)
                .into(holder.ivUserAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserName, tvCommentDate, tvCommentText;
        RatingBar rbCommentRating;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvCommentDate = itemView.findViewById(R.id.tv_comment_date);
            tvCommentText = itemView.findViewById(R.id.tv_comment_text);
            rbCommentRating = itemView.findViewById(R.id.rb_comment_rating);
        }
    }
}
