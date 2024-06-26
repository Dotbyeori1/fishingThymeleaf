package com.haegreen.fishing.entitiy;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ReservationDate")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rdate;

    @Column(unique = true, name = "regDate")
    private LocalDate regDate;

    @Builder.Default
    private int extrasMembers = 16; // 여유인원

    @Builder.Default
    private String fishingSort = "갈치"; // 어업 종류

    private int fishingMoney; // 금액

    @Builder.Default
    private boolean dateModify = false; // 날짜 수정 여부

    // 예약 가능 여부. true면 예약 가능, false면 판매 취소(예약 불가).
    @Builder.Default
    private boolean available = true;

    private String message;

    @Builder.Default
    @OneToMany(mappedBy = "reservationDate")
    private List<Reservation> reservations = new ArrayList<>();
}
