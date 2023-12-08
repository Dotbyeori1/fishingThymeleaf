package com.haegreen.fishing.dto;

import com.haegreen.fishing.entitiy.Reservation;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDateDTO {

    private Long rdate;

    private LocalDate regDate;

    private boolean available;

    private String message;

    private String fishingSort; // 어업 종류

    private Integer extrasMembers; // 여유인원

    private Integer fishingMoney; // 금액

    private boolean dateModify; // 날짜 수정 여부

    private List<ReservationDTO> reservations;
}
