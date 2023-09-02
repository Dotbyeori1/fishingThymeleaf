package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.PageRequestDTO;
import com.haegreen.fishing.dto.PageResultDTO;
import com.haegreen.fishing.dto.ReservationDTO;
import com.haegreen.fishing.entitiy.Reservation;
import com.haegreen.fishing.entitiy.ReservationDate;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.repository.ReservationDateRepository;
import com.haegreen.fishing.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    final ReservationRepository reservationRepository;
    final ReservationDateRepository reservationDateRepository;
    final ReservationDateRepository dateRepository;
    final MemberRepository memberRepository;

    @Override
    public Long register(ReservationDTO dto) {
        Reservation reservation = dtoToEntity(dto);

        // 날짜 정보로부터 ReservationDate 엔티티를 찾는다.
        Optional<ReservationDate> optional = dateRepository.findByRegDate(dto.getRegDate());

        if (optional.isPresent()) {
            ReservationDate reservationDate = optional.get();

            // 예약 가능 여부를 확인한다.
            if (!reservationDate.isAvailable()) {
                throw new IllegalArgumentException("The date is not available for reservation: " + dto.getRegDate());
            }

            reservation.setReservationDate(reservationDate);

        } else {
            throw new IllegalArgumentException("Invalid date: " + dto.getRegDate());
        }

        reservationRepository.save(reservation);

        return reservation.getRvno();
    }

    @Override
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        List<ReservationDTO> reservationDTOS = new ArrayList<>();

        for (Reservation reservation : reservations) {
            ReservationDTO reservationDTO = new ReservationDTO();
            reservationDTO.setRegDate(reservation.getRegDate());
            reservationDTO.setMember(reservation.getMember());

            reservationDTOS.add(reservationDTO);
        }

        return reservationDTOS;
    }

    @Override
    public ReservationDTO get(Long rvno) {
        return null;
    }

    @Override
    public void modify(ReservationDTO reservationDto) {

    }

    @Override
    public void remove(Long rvno) {

    }

    @Override
    public PageResultDTO<ReservationDTO, Object[]> getList(PageRequestDTO pageRequestDTO) {
        return null;
    }
}
