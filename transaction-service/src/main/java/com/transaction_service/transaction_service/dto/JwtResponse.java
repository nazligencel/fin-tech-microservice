package com.transaction_service.transaction_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    public JwtResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
