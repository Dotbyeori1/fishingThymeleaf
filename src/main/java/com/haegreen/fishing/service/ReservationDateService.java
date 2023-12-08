package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.ReservationDateDTO;
import com.haegreen.fishing.entitiy.ReservationDate;

import java.time.LocalDate;
import java.util.List;

public interface ReservationDateService {

    List<ReservationDateDTO> getAllReservationDates(LocalDate startDate, LocalDate endDate) ;
    public ReservationDateDTO getReservationDate(LocalDate regDate);
    public boolean modifyDateAvailable(Long rdate, ReservationDateDTO reservationDateDTO);
    public boolean modifySort(Long rdate, ReservationDateDTO reservationDateDTO);
    public boolean modifySorts(LocalDate startDate, LocalDate endDate, String sort, int extraMembers, int fishingMoney);
    boolean isReservable(ReservationDate reservationDate);

    default ReservationDate DtoToEntity(ReservationDateDTO dto) {
        ReservationDate reservationDate = new ReservationDate();
        reservationDate.setRdate(dto.getRdate());
        reservationDate.setRegDate(dto.getRegDate());
        reservationDate.setMessage(dto.getMessage());
        reservationDate.setAvailable(dto.isAvailable());
        reservationDate.setFishingSort(dto.getFishingSort());
        reservationDate.setExtrasMembers(dto.getExtrasMembers());
        reservationDate.setFishingMoney(dto.getFishingMoney());
        return reservationDate;
    }

    default ReservationDateDTO entityToDto(ReservationDate reservationDate) {
        ReservationDateDTO dto = new ReservationDateDTO();
        dto.setRdate(reservationDate.getRdate());
        dto.setRegDate(reservationDate.getRegDate());
        dto.setMessage(reservationDate.getMessage());
        dto.setAvailable(reservationDate.isAvailable());
        dto.setFishingSort(reservationDate.getFishingSort());
        dto.setExtrasMembers(reservationDate.getExtrasMembers());
        dto.setFishingMoney(reservationDate.getFishingMoney());
        dto.setDateModify(reservationDate.isDateModify());
        return dto;
    }

}
