package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PageableRequest {

    @Builder.Default
    @Max(value = 50, message = "Size cannot be more than 50")
    @Min(value = 0, message = "Size cannot be less than 0")
    private Integer size = 20;

    @Builder.Default
    @Min(value = 0, message = "Page cannot be less than 0")
    private Integer page = 0;

}
