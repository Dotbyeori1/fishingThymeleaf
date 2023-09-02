package com.haegreen.fishing.repository;

import com.haegreen.fishing.entitiy.ReservationDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationDateRepository extends JpaRepository<ReservationDate, Long> {
    Optional<ReservationDate> findByRegDate(LocalDate regDate);


}
