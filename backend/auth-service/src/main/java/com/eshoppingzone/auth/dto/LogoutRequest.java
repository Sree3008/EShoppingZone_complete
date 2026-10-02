package com.eshoppingzone.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {

    @jakarta.validation.constraints.NotNull(message = "Refresh token is required")
    private String refreshToken;

    public LogoutRequest() {
    }

    public LogoutRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
