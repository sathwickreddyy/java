package com.java.lld.oops.configdriven.dataloading.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for user profile data with nested JSON mapping support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class UserProfileDTO {

//    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    private String phone;

    @Size(max = 100, message = "City name cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Country name cannot exceed 100 characters")
    private String country;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Boolean notificationsEnabled;
    private Boolean newsletterSubscribed;

    // Additional fields for extended profile information
    private String state;
    private String zipCode;
    private String street;
    private String customerTier;
}
