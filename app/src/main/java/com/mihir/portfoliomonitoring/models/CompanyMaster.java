package com.mihir.portfoliomonitoring.models;

/**
 * Created by Mihir on 06-08-2017.
 */

public class CompanyMaster {
    private String company_name;
    private String company_sector;
    private long company_score;

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
