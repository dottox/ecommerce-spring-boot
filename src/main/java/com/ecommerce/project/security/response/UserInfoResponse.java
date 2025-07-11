package com.ecommerce.project.security.response;

import lombok.*;

import java.util.List;

// Define a class to represent the login response payload
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String username;
    private List<String> roles;
}


