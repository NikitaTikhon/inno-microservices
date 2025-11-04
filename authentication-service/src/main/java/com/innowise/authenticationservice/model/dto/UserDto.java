package com.innowise.authenticationservice.model.dto;

import com.innowise.authenticationservice.model.RoleEnum;
import com.innowise.authenticationservice.model.entity.Role;
import com.innowise.authenticationservice.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private List<RoleEnum> roles;

    public static UserDto of(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

}
