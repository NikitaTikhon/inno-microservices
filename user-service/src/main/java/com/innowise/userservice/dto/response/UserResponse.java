package com.innowise.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for responding with User details.
 */
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse implements Serializable {

    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;

}