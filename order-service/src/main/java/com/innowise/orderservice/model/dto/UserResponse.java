package com.innowise.orderservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -6335375860345460887L;

    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;

    @Builder.Default
    private List<CardInfoResponse> cardsInfo = new ArrayList<>();

}
