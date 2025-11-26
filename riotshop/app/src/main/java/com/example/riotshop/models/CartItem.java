package com.example.riotshop.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String cartItemId;
    private String accountId; // ID của Account được thêm vào giỏ hàng
    private String userId;      // ID của người dùng sở hữu giỏ hàng
    private Account account;    // Đối tượng Account đầy đủ (đã implement Parcelable)
    private int quantity;       // Số lượng (trong trường hợp bán vật phẩm, nhưng với Acc thì luôn là 1)
    private long totalItemPrice; // Tổng giá cho CartItem này (account.price * quantity)

    public CartItem() {
        // Cần thiết cho Firebase/Database
    }

    // Constructor
    public CartItem(String cartItemId, String accountId, String userId, Account account, int quantity) {
        this.cartItemId = cartItemId;
        this.accountId = accountId;
        this.userId = userId;
        this.account = account;
        this.quantity = quantity;
        calculateTotalItemPrice();
    }

    // Phương thức tính tổng giá
    public void calculateTotalItemPrice() {
        if (account != null) {
            this.totalItemPrice = account.getPrice() * this.quantity;
        } else {
            this.totalItemPrice = 0;
        }
    }

    // Getters and Setters
    public String getCartItemId() { return cartItemId; }
    public String getAccountId() { return accountId; }
    public String getUserId() { return userId; }
    public Account getAccount() { return account; }
    public int getQuantity() { return quantity; }
    public long getTotalItemPrice() { return totalItemPrice; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // --- TRIỂN KHAI PARCELABLE ---
    protected CartItem(Parcel in) {
        cartItemId = in.readString();
        accountId = in.readString();
        userId = in.readString();
        account = in.readParcelable(Account.class.getClassLoader());
        quantity = in.readInt();
        totalItemPrice = in.readLong();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cartItemId);
        dest.writeString(accountId);
        dest.writeString(userId);
        dest.writeParcelable(account, flags);
        dest.writeInt(quantity);
        dest.writeLong(totalItemPrice);
    }
}