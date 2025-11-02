package com.innowise.authenticationservice.model.dto;

import com.innowise.authenticationservice.model.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TokenInfoResponse {

    private Long userId;

    private List<RoleEnum> roles;

}
