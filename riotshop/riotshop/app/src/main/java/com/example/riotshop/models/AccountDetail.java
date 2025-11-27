package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class AccountDetail {
    @SerializedName("accDetailId")
    private int accDetailId;
    
    @SerializedName("accountUsername")
    private String accountUsername;
    
    @SerializedName("accountPassword")
    private String accountPassword;
    
    @SerializedName("riotId")
    private String riotId;
    
    @SerializedName("recoveryEmail")
    private String recoveryEmail;
    
    @SerializedName("productTemplate")
    private ProductTemplate productTemplate;

    // Getters
    public int getAccDetailId() {
        return accDetailId;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public String getRiotId() {
        return riotId;
    }

    public String getRecoveryEmail() {
        return recoveryEmail;
    }

    public ProductTemplate getProductTemplate() {
        return productTemplate;
    }
}

