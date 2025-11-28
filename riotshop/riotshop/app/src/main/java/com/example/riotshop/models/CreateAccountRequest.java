package com.example.riotshop.models;

import com.google.gson.annotations.SerializedName;

public class CreateAccountRequest {
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
    
    public CreateAccountRequest(String accountUsername, String accountPassword, String riotId, 
                               String recoveryEmail, Boolean isDropMail, Double originalPrice) {
        this.accountUsername = accountUsername;
        this.accountPassword = accountPassword;
        this.riotId = riotId;
        this.recoveryEmail = recoveryEmail;
        this.isDropMail = isDropMail;
        this.originalPrice = originalPrice;
    }
    
    // Getters
    public String getAccountUsername() { return accountUsername; }
    public String getAccountPassword() { return accountPassword; }
    public String getRiotId() { return riotId; }
    public String getRecoveryEmail() { return recoveryEmail; }
    public Boolean getIsDropMail() { return isDropMail; }
    public Double getOriginalPrice() { return originalPrice; }
}

