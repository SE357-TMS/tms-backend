package com.example.tms.dto.request;

import java.time.LocalDate;

import com.example.tms.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String phoneNumber;
    private String address;
    private LocalDate birthday;
    private User.Gender gender;
}

