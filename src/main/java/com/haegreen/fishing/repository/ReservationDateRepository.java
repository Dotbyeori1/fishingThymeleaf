package com.haegreen.fishing.repository;

import com.haegreen.fishing.entitiy.ReservationDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationDateRepository extends JpaRepository<ReservationDate, Long> {

    @Query("select r from ReservationDate r where r.regDate = :regDate")
    ReservationDate findReservationDateByRegDate(@Param("regDate") LocalDate regDate);

    @Query("select r from ReservationDate r where r.regDate between :startDate and :endDate")
    List<ReservationDate> findBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<ReservationDate> findAllByRegDateBefore(LocalDate date);

    Optional<ReservationDate> findByRegDate(LocalDate regDate);

}
