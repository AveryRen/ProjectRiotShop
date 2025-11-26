package com.example.riotshop.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private String name;
    private String price;
    private int image;
    private String description;

    public Product(String name, String price, int image, String description) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
    }

    // Parcelable implementation
    protected Product(Parcel in) {
        name = in.readString();
        price = in.readString();
        image = in.readInt();
        description = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(price);
        dest.writeInt(image);
        dest.writeString(description);
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public int getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }
}
