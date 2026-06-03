package com.schoolproject.app.dto;

import com.schoolproject.app.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String userId;
    private Role role;
    private String message;
}
