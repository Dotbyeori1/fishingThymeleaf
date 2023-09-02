package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.PageRequestDTO;
import com.haegreen.fishing.dto.PageResultDTO;
import com.haegreen.fishing.dto.ReservationDTO;
import com.haegreen.fishing.entitiy.Reservation;

import java.util.List;

public interface ReservationService {

    Long register(ReservationDTO dto); // 등록
    ReservationDTO get(Long rvno); // 조회
    void modify(ReservationDTO reservationDto); //수정
    void remove(Long rvno); // 삭제
    PageResultDTO<ReservationDTO, Object[]> getList(PageRequestDTO pageRequestDTO); // 리스트 출력
    public List<ReservationDTO> getAllReservations();

    default Reservation dtoToEntity(ReservationDTO dto){

        Reservation reservation = Reservation.builder()
                .rvno(dto.getRvno())
                .message(dto.getMessage())
                .member(dto.getMember())
                .tel(dto.getTel())
                .regDate(dto.getRegDate())
                .regName(dto.getRegName())
                .email(dto.getEmail())
                .depositName(dto.getDepositName())
                .build();

        return  reservation;
    }

    default ReservationDTO entityToDto(Reservation reservation){

        ReservationDTO reservationDto = ReservationDTO.builder()
                .rvno(reservation.getRvno())
                .tel(reservation.getTel())
                .message(reservation.getMessage())
                .member(reservation.getMember())
                .email(reservation.getEmail())
                .regName(reservation.getRegName())
                .regDate(reservation.getRegDate())
                .depositName(reservation.getDepositName())
                .state(reservation.isState())
                .cancel(reservation.isCancel())
                .build();

        return reservationDto;
    }

}
