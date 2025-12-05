package com.example.tms.dto.response.booking;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelerResponse {
    
    private UUID id;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String identityDoc;
}
