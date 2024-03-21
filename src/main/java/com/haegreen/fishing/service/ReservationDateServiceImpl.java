package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.ReservationDateDTO;
import com.haegreen.fishing.entitiy.ReservationDate;
import com.haegreen.fishing.repository.ReservationDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationDateServiceImpl implements ReservationDateService {

    private final ReservationDateRepository reservationDateRepository;

    @PostConstruct
    public void init() {
        // 애플리케이션이 시작된 후 1분 후에 처음으로 generateReservationDates() 메소드를 호출합니다.
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                generateReservationDates();
                beforeTodayModify();
            }
        }, 1000 * 60);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행됩니다.
    public void generateReservationDates() {
        LocalDate today = LocalDate.now();
        LocalDate forMonthLater = today.plusMonths(4);

        for (LocalDate regDate = today; !regDate.isAfter(forMonthLater); regDate = regDate.plusDays(1)) {
            Optional<ReservationDate> optional = reservationDateRepository.findByRegDate(regDate);

            if (optional.isEmpty()) { // 해당 날짜의 ReservationDate가 없으면 새로 만듭니다.
                ReservationDate reservationDate = new ReservationDate();
                reservationDate.setRegDate(regDate);
                reservationDate.setAvailable(true);

                reservationDateRepository.save(reservationDate);
            }
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 5 * * ?") // 매일 오전 5시 실행  // mm - dd - 16
    public void beforeTodayModify() {
        LocalDate today = LocalDate.now();

        // 오늘의 ReservationDate 객체를 찾아서 예약 불가능하게 설정
        Optional<ReservationDate> todayReservationDate = reservationDateRepository.findByRegDate(today);
        if (todayReservationDate.isPresent()) {
            ReservationDate reservationDate = todayReservationDate.get();
            reservationDate.setAvailable(false);
            reservationDateRepository.save(reservationDate);
        }
    }

    @Override
    public List<ReservationDateDTO> getAllReservationDates(LocalDate startDate, LocalDate endDate){
        try {
            List<ReservationDate> reservationDates = reservationDateRepository.findBetweenDates(startDate, endDate);

            return reservationDates.stream()
                    .map(this::entityToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 로깅, 에러 메시지 반환 등
            throw new RuntimeException("Error while fetching data", e);  // 예외를 다시 던져서 컨트롤러에서 처리하게 함
        }
    }

    @Transactional // 트랜잭션 처리 추가
    @Override
    public boolean modifyDateAvailable(Long rdate, ReservationDateDTO reservationDateDTO){
        Optional<ReservationDate> optionalReservationDate = reservationDateRepository.findById(rdate);
        if (optionalReservationDate.isPresent()) {
            ReservationDate reservationDate = optionalReservationDate.get();
            reservationDate.setAvailable(reservationDateDTO.isAvailable());
            reservationDate.setMessage(reservationDateDTO.getMessage());
            if(!reservationDateDTO.isAvailable()) { // 이용불가 -> 수정 // 이용가능 -> 수정안함 으로 되돌리기
                reservationDate.setDateModify(true);
            }else {
                reservationDate.setDateModify(false);
            }
            reservationDateRepository.save(reservationDate);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ReservationDateDTO getReservationDate(LocalDate regDate){
        ReservationDate reservationDate = reservationDateRepository.findReservationDateByRegDate(regDate);
        if (reservationDate != null) {
            return entityToDto(reservationDate);
        } else {
            return null;
        }
    }

    @Transactional // 트랜잭션 처리 추가
    @Override
    public boolean modifySort(Long rdate, ReservationDateDTO reservationDateDTO){
        Optional<ReservationDate> optionalReservationDate = reservationDateRepository.findById(rdate);
        if (optionalReservationDate.isPresent()) {
            ReservationDate reservationDate = optionalReservationDate.get();
            reservationDate.setFishingMoney(reservationDateDTO.getFishingMoney());
            reservationDate.setFishingSort(reservationDateDTO.getFishingSort());
            reservationDate.setExtrasMembers(reservationDateDTO.getExtrasMembers());
            reservationDateRepository.save(reservationDate);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isReservable(ReservationDate reservationDate) {
        String fishingSort = reservationDate.getFishingSort();
        if (fishingSort != null && ("갈치".contains(fishingSort)) && LocalTime.now().isBefore(LocalTime.of(14, 0))) {
            reservationDate.setAvailable(true);
            return true;
        }else if(!reservationDate.isAvailable() || reservationDate.isDateModify()) {
            reservationDate.setAvailable(false);
            return false;
        }
        return true;
    }

    @Transactional
    @Override
    public boolean modifySorts(LocalDate startDate, LocalDate endDate, String sort, int extraMembers, int fishingMoney) {
        List<ReservationDateDTO> reservationDatesDTOs = getAllReservationDates(startDate, endDate);

        // DTO를 Entity로 변환
        List<ReservationDate> reservationDates = reservationDatesDTOs.stream()
                .map(this::DtoToEntity)
                .collect(Collectors.toList());

        for (ReservationDate reservationDate : reservationDates) {
            reservationDate.setFishingSort(sort);
            reservationDate.setExtrasMembers(extraMembers);
            reservationDate.setFishingMoney(fishingMoney);
        }

        // 변경된 Entity들 저장
        try {
            reservationDateRepository.saveAll(reservationDates);
            return true;
        } catch (Exception e) {
            // 로깅, 에러 메시지 반환 등
            throw new RuntimeException("Error while saving data", e);
        }
    }


}
