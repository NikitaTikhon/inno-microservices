package com.innowise.userservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for responding with CardInfo details.
 */
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CardInfoResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -1471526800410538319L;

    private Long id;
    private String number;
    private String holder;
    private LocalDate expirationDate;

}

