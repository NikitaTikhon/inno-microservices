package com.innowise.userservice.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * DTO for creating or updating a CardInfo.
 */
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CardInfoRequest {

    @NotNull(message = "User id cannot be null")
    private Long userId;

    @NotBlank(message = "Card number cannot be blank")
    @Size(max = 50, message = "Card number must not exceed 50 characters")
    private String number;

    @NotBlank(message = "Holder name cannot be blank")
    @Size(max = 50, message = "Holder name must not exceed 50 characters")
    private String holder;

    @NotNull(message = "Expiration date cannot be null")
    @FutureOrPresent(message = "Expiration date must be in the present or future")
    private LocalDate expirationDate;

}
