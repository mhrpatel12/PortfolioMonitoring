package com.mihir.portfoliomonitoring.models;

/**
 * Created by Mihir on 07-08-2017.
 */

public class ReferralCodeMaster {
    private String referralCode;
    private int isActive;

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }
}
