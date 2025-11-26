package com.example.riotshop.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Account implements Parcelable {
    private String id;
    private String name; // Tên hiển thị/Tiêu đề của Acc
    private long price;
    private String category; // Có thể là "Acc Siêu Vip", "Acc Rank Đồng"...
    private int imageResId;
    private float rating;

    // 🔑 Thuộc tính CỤ THỂ cho Acc Game
    private String rank; // Ví dụ: "Kim Cương IV"
    private int skinsOwned; // Số lượng trang phục
    private int championsOwned; // Số lượng tướng

    // Constructor ĐẦY ĐỦ
    public Account(String id, String name, long price, String category, int imageResId, float rating, String rank, int skinsOwned, int championsOwned) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageResId = imageResId;
        this.rating = rating;
        this.rank = rank;
        this.skinsOwned = skinsOwned;
        this.championsOwned = championsOwned;
    }

    // Constructor rỗng (Cần thiết cho Firebase/Database)
    public Account() {}

    // 🔑 Getters (Bắt buộc)
    public String getId() { return id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getCategory() { return category; }
    public int getImageResId() { return imageResId; }
    public float getRating() { return rating; }
    public String getRank() { return rank; }
    public int getSkinsOwned() { return skinsOwned; }
    public int getChampionsOwned() { return championsOwned; }

    // ... (Thêm Setters nếu cần)

    // --- TRIỂN KHAI PARCELABLE ---
    protected Account(Parcel in) {
        id = in.readString();
        name = in.readString();
        price = in.readLong();
        category = in.readString();
        imageResId = in.readInt();
        rating = in.readFloat();
        rank = in.readString();
        skinsOwned = in.readInt();
        championsOwned = in.readInt();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeLong(price);
        dest.writeString(category);
        dest.writeInt(imageResId);
        dest.writeFloat(rating);
        dest.writeString(rank);
        dest.writeInt(skinsOwned);
        dest.writeInt(championsOwned);
    }
}