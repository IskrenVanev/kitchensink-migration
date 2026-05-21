package com.iskren.dto;

public class RegisterRequestDTO {
     private String username;

    private String password;

    private String memberId;

    // getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getMemberId() {
        return memberId;
    }

    // setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
