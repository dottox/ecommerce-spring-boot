package com.ecommerce.project.security.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Defifnes a class to represent the login request payload
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
