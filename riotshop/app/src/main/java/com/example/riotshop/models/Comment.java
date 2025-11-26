package com.example.riotshop.models;

public class Comment {
    private String commentId;
    private String accountId; // ID của sản phẩm (Account) được bình luận
    private String userId;
    private String userName;
    private String userAvatarUrl; // URL ảnh đại diện của người dùng
    private float rating; // Điểm đánh giá (ví dụ: từ 1 đến 5 sao)
    private String text;
    private long timestamp;

    public Comment() {
        // Constructor trống cho Firebase
    }

    public Comment(String commentId, String accountId, String userId, String userName, String userAvatarUrl, float rating, String text, long timestamp) {
        this.commentId = commentId;
        this.accountId = accountId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
        this.rating = rating;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters
    public String getCommentId() { return commentId; }
    public String getAccountId() { return accountId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatarUrl() { return userAvatarUrl; }
    public float getRating() { return rating; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setCommentId(String commentId) { this.commentId = commentId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }
    public void setRating(float rating) { this.rating = rating; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
