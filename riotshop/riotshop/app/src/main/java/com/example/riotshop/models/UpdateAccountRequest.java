package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class UpdateAccountRequest {
    @SerializedName("accountUsername")
    private String accountUsername;
    
    @SerializedName("accountPassword")
    private String accountPassword;
    
    @SerializedName("riotId")
    private String riotId;
    
    @SerializedName("recoveryEmail")
    private String recoveryEmail;
    
    @SerializedName("isDropMail")
    private Boolean isDropMail;
    
    @SerializedName("originalPrice")
    private Double originalPrice;
    
    @SerializedName("isSold")
    private Boolean isSold;
    
    public UpdateAccountRequest(String accountUsername, String accountPassword, String riotId, 
                               String recoveryEmail, Boolean isDropMail, Double originalPrice, Boolean isSold) {
        this.accountUsername = accountUsername;
        this.accountPassword = accountPassword;
        this.riotId = riotId;
        this.recoveryEmail = recoveryEmail;
        this.isDropMail = isDropMail;
        this.originalPrice = originalPrice;
        this.isSold = isSold;
    }
    
    // Getters
    public String getAccountUsername() { return accountUsername; }
    public String getAccountPassword() { return accountPassword; }
    public String getRiotId() { return riotId; }
    public String getRecoveryEmail() { return recoveryEmail; }
    public Boolean getIsDropMail() { return isDropMail; }
    public Double getOriginalPrice() { return originalPrice; }
    public Boolean getIsSold() { return isSold; }
}

