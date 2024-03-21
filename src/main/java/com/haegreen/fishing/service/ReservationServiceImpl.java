package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.PageRequestDTO;
import com.haegreen.fishing.dto.PageResultDTO;
import com.haegreen.fishing.dto.ReservationDTO;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.entitiy.Reservation;
import com.haegreen.fishing.entitiy.ReservationDate;
import com.haegreen.fishing.repository.ReservationDateRepository;
import com.haegreen.fishing.repository.ReservationRepository;
import com.haegreen.fishing.security.CustomUserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationDateService reservationDateService;
    private final ReservationDateRepository reservationDateRepository;
    private final ReservationDateRepository dateRepository;

    @PostConstruct
    public void init() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                deleteOldReservationsAndDates();
            }
        }, 1000 * 60);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldReservationsAndDates() {
        LocalDate today = LocalDate.now();
        LocalDate sixMonthsAgo = today.minusMonths(6L);
        List<Reservation> oldReservations = reservationRepository.findAllByRegDateBefore(sixMonthsAgo);
        reservationRepository.deleteAll(oldReservations);
        List<ReservationDate> oldDates = reservationDateRepository.findAllByRegDateBefore(sixMonthsAgo);
        reservationDateRepository.deleteAll(oldDates);
    }

    @Override
    public Long register(ReservationDTO dto) {
        Reservation reservation = dtoToEntity(dto);

        // 등록되지 않는 예약 날짜 체크
        Optional<ReservationDate> optional = dateRepository.findByRegDate(dto.getRegDate());
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("등록되지 않는 예약 날짜 입니다 : " + dto.getRegDate());
        }
        ReservationDate reservationDate = optional.get();

        // 예약 불가능한 상태 체크
        if (!reservationDateService.isReservable(reservationDate)) {
            throw new IllegalArgumentException("예약불가능 한 상태 입니다 : " + dto.getRegDate());
        }

        reservation.setReservationDate(reservationDate);
        reservationRepository.save(reservation);
        return reservation.getRvno();
    }

    @Override
    public List<ReservationDTO> getAllReservations(LocalDate startDate, LocalDate endDate) {
        List<Reservation> reservations = reservationRepository.findBetweenReservation(startDate, endDate);
        List<ReservationDTO> reservationDTOS = new ArrayList<>();
        for (Reservation reservation : reservations) {
            ReservationDTO reservationDTO = new ReservationDTO();
            reservationDTO.setRegDate(String.valueOf(reservation.getRegDate()));
            reservationDTO.setMember(reservation.getMember());
            reservationDTO.setState(reservation.isState());
            String regName = reservation.getRegName();
            StringBuilder changeName = new StringBuilder();
            if (regName.length() <= 2) {
                changeName.append(regName);
            } else {
                changeName.append(regName.charAt(0));
                changeName.append("*".repeat(regName.length() - 2));
                changeName.append(regName.charAt(regName.length() - 1));
            }
            reservationDTO.setRegName(changeName.toString());
            reservationDTOS.add(reservationDTO);
        }
        return reservationDTOS;
    }

    @Override
    public List<ReservationDTO> getUserReservation(Authentication authentication, ReservationDTO reservationDTO) {
        List<ReservationDTO> reservationDTOS = new ArrayList<>();
        List<Reservation> reservations = new ArrayList<>();
        if (authentication != null && authentication.isAuthenticated()) {
            Member member = ((CustomUserDetails) authentication.getPrincipal()).getMember();
            reservations = reservationRepository.findReservationsByEmail(member.getEmail());
        } else if (reservationDTO != null) {
            reservations = reservationRepository.findReservationsByRegNameAndTel(reservationDTO
                    .getRegName(), reservationDTO
                    .getTel());
        }
        for (Reservation reservation : reservations)
            reservationDTOS.add(entityToDto(reservation));
        return reservationDTOS;
    }

    @Override
    public Optional<Reservation> check(String email, String regName, String tel) {
        List<Reservation> results = reservationRepository.findReservationsByRegNameAndTel(regName, tel);
        return results.isEmpty() ? Optional.empty() : Optional.ofNullable(results.get(0));
    }

    @Override
    public List<ReservationDTO> getDateUserReservation(LocalDate searchDate) {
        List<ReservationDTO> reservationDTOS = new ArrayList<>();
        List<Reservation> reservations = reservationRepository.findByDate(searchDate);
        for (Reservation reservation : reservations)
            reservationDTOS.add(entityToDto(reservation));
        return reservationDTOS;
    }

    @Override
    @Transactional
    public Optional<Reservation> modifyState(Long rvno, ReservationDTO reservationDTO) {
        Optional<Reservation> reservation = reservationRepository.findById(rvno);
        reservation.get().setState(reservationDTO.isState());
        reservationRepository.save(reservation.get());
        return reservation;
    }

    public ReservationDTO get(Long rvno) {
        Reservation reservation = reservationRepository.findById(rvno).orElseThrow(() -> new IllegalArgumentException("조회가 안됨 : " + rvno));
        ReservationDTO reservationDTO = entityToDto(reservation);
        return reservationDTO;
    }

    @Override
    @Transactional
    public void modify(ReservationDTO reservationDto) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservationDto.getRvno());
        if (optionalReservation.isPresent()) {
            Reservation reservation = optionalReservation.get();
            reservation.setTel(reservationDto.getTel());
            reservation.setMember(reservationDto.getMember());
            reservation.setRegName(reservationDto.getRegName());
            reservation.setDepositName(reservationDto.getDepositName());
            reservation.setRegion(reservationDto.getRegion());
            reservation.setRegDate(reservationDto.getRegDate());
            reservationRepository.save(reservation);
        } else {
            throw new IllegalArgumentException("수정실패 : " + reservationDto.getRvno());
        }
    }

    @Override
    @Transactional
    public boolean modifyMoney(ReservationDTO reservationDto) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(reservationDto.getRvno());
        if (optionalReservation.isPresent()) {
            Reservation reservation = optionalReservation.get();
            reservation.setMoney(reservationDto.getMoney());
            reservationRepository.save(reservation);
            return true;
        }
        throw new IllegalArgumentException("" + reservationDto.getRvno());
    }

    @Transactional
    public Boolean remove(Long rvno) {
        try {
            reservationRepository.deleteById(rvno);
            return Boolean.TRUE;
        } catch (EmptyResultDataAccessException e) {
            return Boolean.FALSE;
        }
    }

    @Override
    public PageResultDTO<ReservationDTO, Object[]> getSearchList(PageRequestDTO pageRequestDTO, Long rvno, String regName, String depositName, String tel, LocalDate regDate) {
        Sort sort = Sort.by(Sort.Direction.DESC, "rvno");
        PageRequest pageRequest = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(), sort);
        Page<Object[]> result = reservationRepository.searchPage(rvno, regName, depositName, tel, regDate, pageRequest);
        Function<Object[], ReservationDTO> fn = objectArr -> {
            Reservation reservation = (Reservation) objectArr[0];
            Integer extraMembers = (Integer) objectArr[1];

            ReservationDTO dto = entityToDto(reservation);
            dto.setExtraMembers(extraMembers);
            return dto;
        };
        return new PageResultDTO<>(result, fn);
    }
}
