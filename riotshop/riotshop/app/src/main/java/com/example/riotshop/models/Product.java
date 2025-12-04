package com.example.riotshop.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    private int templateId;
    private String name;
    private String price;
    private int image;
    private String imageUrl; // URL của ảnh từ server
    private String description;

    public Product(String name, String price, int image, String description) {
        this.templateId = 0;
        this.name = name;
        this.price = price;
        this.image = image;
        this.imageUrl = null;
        this.description = description;
    }

    public Product(int templateId, String name, String price, int image, String description) {
        this.templateId = templateId;
        this.name = name;
        this.price = price;
        this.image = image;
        this.imageUrl = null;
        this.description = description;
    }

    public Product(int templateId, String name, String price, int image, String imageUrl, String description) {
        this.templateId = templateId;
        this.name = name;
        this.price = price;
        this.image = image;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // Parcelable implementation
    protected Product(Parcel in) {
        templateId = in.readInt();
        name = in.readString();
        price = in.readString();
        image = in.readInt();
        imageUrl = in.readString();
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
        dest.writeInt(templateId);
        dest.writeString(name);
        dest.writeString(price);
        dest.writeInt(image);
        dest.writeString(imageUrl);
        dest.writeString(description);
    }

    // Getters
    public int getTemplateId() {
        return templateId;
    }

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
