package com.example.riotshop.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Address implements Parcelable {
    private String addressId;
    private String userId;
    private String recipientName; // Tên người nhận
    private String phoneNumber;
    private String streetAddress; // Địa chỉ chi tiết (số nhà, tên đường)
    private String ward;          // Phường/Xã
    private String district;      // Quận/Huyện
    private String city;          // Tỉnh/Thành phố
    private boolean isDefault;    // Đánh dấu địa chỉ mặc định

    public Address() {
    }

    public Address(String addressId, String userId, String recipientName, String phoneNumber, String streetAddress, String ward, String district, String city, boolean isDefault) {
        this.addressId = addressId;
        this.userId = userId;
        this.recipientName = recipientName;
        this.phoneNumber = phoneNumber;
        this.streetAddress = streetAddress;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.isDefault = isDefault;
    }

    // Getters
    public String getAddressId() { return addressId; }
    public String getUserId() { return userId; }
    public String getRecipientName() { return recipientName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getStreetAddress() { return streetAddress; }
    public String getWard() { return ward; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public boolean isDefault() { return isDefault; }

    // --- TRIỂN KHAI PARCELABLE ---
    protected Address(Parcel in) {
        addressId = in.readString();
        userId = in.readString();
        recipientName = in.readString();
        phoneNumber = in.readString();
        streetAddress = in.readString();
        ward = in.readString();
        district = in.readString();
        city = in.readString();
        isDefault = in.readByte() != 0;
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(addressId);
        dest.writeString(userId);
        dest.writeString(recipientName);
        dest.writeString(phoneNumber);
        dest.writeString(streetAddress);
        dest.writeString(ward);
        dest.writeString(district);
        dest.writeString(city);
        dest.writeByte((byte) (isDefault ? 1 : 0));
    }
}