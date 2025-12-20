package com.example.tms.dto.response.customer;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponse {
    private UUID userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String avatarUrl;
}

