package com.dusanbranovic.bookme.auth;

import com.dusanbranovic.bookme.models.UserType;

public class AuthResponse {

    private Long id;
    private UserType userType;
    private String token;

    public AuthResponse(Long id, UserType userType, String token) {
        this.id = id;
        this.userType = userType;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
