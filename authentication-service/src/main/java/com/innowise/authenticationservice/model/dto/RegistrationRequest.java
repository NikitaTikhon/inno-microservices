package com.innowise.authenticationservice.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Surname cannot be blank")
    @Size(max = 50, message = "Surname must not exceed 50 characters")
    private String surname;

    @NotNull(message = "Birth date cannot be null")
    @PastOrPresent(message = "Birth date must be in the past or present")
    private LocalDate birthDate;

    @Email
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 64, message = "Password must be at least 6 and no more than 64 characters")
    private String password;

}
