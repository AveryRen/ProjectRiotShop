package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class AvailableAccount {
    @SerializedName("accDetailId")
    private int accDetailId;
    
    @SerializedName("accountUsername")
    private String accountUsername;
    
    @SerializedName("riotId")
    private String riotId;
    
    @SerializedName("productTemplate")
    private ProductTemplate productTemplate;

    public int getAccDetailId() {
        return accDetailId;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public String getRiotId() {
        return riotId;
    }

    public ProductTemplate getProductTemplate() {
        return productTemplate;
    }
}

