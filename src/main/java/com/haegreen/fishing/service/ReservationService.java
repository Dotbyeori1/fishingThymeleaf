package com.haegreen.fishing.service;


import com.haegreen.fishing.dto.PageRequestDTO;
import com.haegreen.fishing.dto.PageResultDTO;
import com.haegreen.fishing.dto.ReservationDTO;
import com.haegreen.fishing.entitiy.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;

public interface ReservationService {
    Long register(ReservationDTO paramReservationDTO);

    ReservationDTO get(Long paramLong);

    void modify(ReservationDTO paramReservationDTO);

    boolean modifyMoney(ReservationDTO paramReservationDTO);

    Boolean remove(Long paramLong);

    List<ReservationDTO> getAllReservations(LocalDate paramLocalDate1, LocalDate paramLocalDate2);

    List<ReservationDTO> getUserReservation(Authentication paramAuthentication, ReservationDTO paramReservationDTO);

    List<ReservationDTO> getDateUserReservation(LocalDate paramLocalDate);

    Optional<Reservation> check(String paramString1, String paramString2, String paramString3);

    Optional<Reservation> modifyState(Long paramLong, ReservationDTO paramReservationDTO);

    PageResultDTO<ReservationDTO, Object[]> getSearchList(PageRequestDTO paramPageRequestDTO, Long paramLong, String paramString1, String paramString2, String paramString3, LocalDate paramLocalDate);

    default Reservation dtoToEntity(ReservationDTO dto) {
        Reservation reservation = Reservation.builder().
                rvno(dto.getRvno()).
                message(dto.getMessage()).
                region(dto.getRegion()).
                member(dto.getMember()).
                tel(dto.getTel()).
                regDate(dto.getRegDate()).
                regName(dto.getRegName()).
                email(dto.getEmail()).
                depositName(dto.getDepositName()).
                money(dto.getMoney()).
                build();
        return reservation;
    }

    default ReservationDTO entityToDto(Reservation reservation) {
        ReservationDTO reservationDto = ReservationDTO.builder()
                .rvno(reservation.getRvno())
                .tel(reservation.getTel())
                .message(reservation.getMessage())
                .region(reservation.getRegion())
                .member(reservation.getMember())
                .email(reservation.getEmail())
                .regName(reservation.getRegName())
                .regDate(reservation.getRegDate())
                .depositName(reservation.getDepositName())
                .state(reservation.isState())
                .money(reservation.getMoney())
                .build();
        return reservationDto;
    }
}