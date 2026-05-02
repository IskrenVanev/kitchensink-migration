package com.iskren.dto;

public class MemberStatsDTO {
    private String domain;
    private int count;

    public MemberStatsDTO(String domain, int count) {
        this.domain = domain;
        this.count = count;
    }   

    public String getDomain() {
        return domain;
    }

    public int getCount() {
        return count;
    }
}
