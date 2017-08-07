package com.mihir.portfoliomonitoring.models;

/**
 * Created by Mihir on 06-08-2017.
 */

public class User {
    private String userID;
    private String user_name;
    private String user_email;
    private String company_name;
    private String company_sector;
    private long company_score;
    private int isReferral;

    public User() {

    }

    public User(String userID, String user_name, String user_email, String company_name, String company_sector, long company_score, int isReferral) {
        this.userID = userID;
        this.user_name = user_name;
        this.user_email = user_email;
        this.company_name = company_name;
        this.company_sector = company_sector;
        this.company_score = company_score;
        this.isReferral = isReferral;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public String getCompany_sector() {
        return company_sector;
    }

    public void setCompany_sector(String company_sector) {
        this.company_sector = company_sector;
    }

    public long getCompany_score() {
        return company_score;
    }

    public void setCompany_score(long company_score) {
        this.company_score = company_score;
    }
}
