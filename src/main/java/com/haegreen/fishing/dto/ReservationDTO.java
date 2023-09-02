package com.haegreen.fishing.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {

    private Long rvno; //예약번호
    private String message; //요청사항
    private String email;

    private String tel; // 전화번호
    private String tel1, tel2, tel3; // 전화번호 sprit
    private int member; // 인원수

    private LocalDate regDate; // 예약날짜

    private LocalDateTime regTime; // 등록일

    private String regName; // 예약자명
    private String depositName; // 입금자 명

    private int money; // 돈

    private boolean state;
    private boolean cancel;
}
