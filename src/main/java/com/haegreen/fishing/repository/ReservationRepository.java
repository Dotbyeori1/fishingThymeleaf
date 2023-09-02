package com.haegreen.fishing.repository;

import com.haegreen.fishing.entitiy.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select r from Reservation r where r.regName = :name and r.tel = :tel")
    List<Reservation> findReservationsByRegNameAndTel(@Param("name") String regName, @Param("tel") String tel);

    @Query("select r from Reservation r where r.regDate = :regDate")
    List<Reservation> findByDate(@Param("regDate")LocalDate regDate);

}
