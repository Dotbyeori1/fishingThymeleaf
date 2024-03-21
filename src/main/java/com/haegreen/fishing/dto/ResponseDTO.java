package com.haegreen.fishing.dto;

import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Data
public class ResponseDTO<T> {
    private String error;
    private String success;
    private List<T> data;
}
