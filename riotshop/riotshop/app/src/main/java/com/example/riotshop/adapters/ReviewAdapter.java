package com.example.riotshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.models.Review;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        
        holder.ratingBar.setRating(review.getRating());
        holder.tvComment.setText(review.getComment() != null ? review.getComment() : "");
        
        // Get username - backend trả về username trực tiếp hoặc trong user object
        String username = review.getUsername();
        holder.tvUsername.setText(username != null ? username : "Người dùng");
        
        // Parse date từ string
        if (review.getCreatedAt() != null && !review.getCreatedAt().isEmpty()) {
            try {
                // Backend trả về format ISO 8601 hoặc format khác
                // Thử parse với nhiều format
                String dateStr = review.getCreatedAt();
                
                // Nếu có "T" thì là ISO format
                if (dateStr.contains("T")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date date = sdf.parse(dateStr);
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    holder.tvDate.setText(displayFormat.format(date));
                } else {
                    // Thử format khác hoặc hiển thị trực tiếp
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = sdf.parse(dateStr);
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    holder.tvDate.setText(displayFormat.format(date));
                }
            } catch (Exception e) {
                // Nếu không parse được, hiển thị trực tiếp
                holder.tvDate.setText(review.getCreatedAt());
            }
        } else {
            holder.tvDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvComment, tvDate;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_review_username);
            tvComment = itemView.findViewById(R.id.tv_review_comment);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            ratingBar = itemView.findViewById(R.id.rating_bar_review);
        }
    }
}

