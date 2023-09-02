package com.haegreen.fishing.entitiy;

import lombok.*;

import javax.persistence.*;
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

    // 예약 가능 여부. true면 예약 가능, false면 판매 취소(예약 불가).
    @Builder.Default
    private boolean available = true;

    @OneToMany(mappedBy = "reservationDate")
    private List<Reservation> reservations = new ArrayList<>();
}
