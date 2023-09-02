package com.haegreen.fishing.service;

import com.haegreen.fishing.entitiy.ReservationDate;
import com.haegreen.fishing.repository.ReservationDateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class ReservationDateService {

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @PostConstruct
    public void init() {
        // 애플리케이션이 시작된 후 1분 후에 처음으로 generateReservationDates() 메소드를 호출합니다.
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                generateReservationDates();
            }
        }, 1000 * 60);
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행됩니다.
    public void generateReservationDates() {
        LocalDate today = LocalDate.now();
        LocalDate oneYearLater = today.plusYears(1);

        for (LocalDate regDate = today; !regDate.isAfter(oneYearLater); regDate = regDate.plusDays(1)) {
            Optional<ReservationDate> optional = reservationDateRepository.findByRegDate(regDate);

            if (!optional.isPresent()) { // 해당 날짜의 ReservationDate가 없으면 새로 만듭니다.
                ReservationDate reservationDate = new ReservationDate();
                reservationDate.setRegDate(regDate);
                reservationDate.setAvailable(true);

                reservationDateRepository.save(reservationDate);
            }
        }
    }
}
