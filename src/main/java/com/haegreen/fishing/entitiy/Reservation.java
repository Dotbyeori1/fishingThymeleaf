package com.haegreen.fishing.entitiy;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Reservation")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "regName")
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rvno; //예약번호

    private LocalDate regDate; // 예약날짜

    @Column(length = 200)
    private String message; //요청사항

    private String tel; // 전화번호

    private int member; // 인원수

    private String regName; // 예약자명
    private String depositName; // 입금자 명

    private String email; // 이메일


    @Builder.Default
    private boolean state = false; // 예약 상태 (확정 전, 확정 완료)

    @Builder.Default
    private boolean cancel = false; // 취소 여부 (취소 전, 취소 후)

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationDate reservationDate;

}
