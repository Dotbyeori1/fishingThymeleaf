package com.haegreen.fishing.repository;


import com.haegreen.fishing.entitiy.Reservation;
import com.haegreen.fishing.repository.search.SearchBoardRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, QuerydslPredicateExecutor<Reservation>, SearchBoardRepository {
    @Query("select r from Reservation r where r.regName = :name and r.tel = :tel")
    List<Reservation> findReservationsByRegNameAndTel(@Param("name") String paramString1, @Param("tel") String paramString2);

    @Query("select r from Reservation r where r.regDate between :startDate and :endDate")
    List<Reservation> findBetweenReservation(@Param("startDate") LocalDate paramLocalDate1, @Param("endDate") LocalDate paramLocalDate2);

    @Query("select r from Reservation r where r.email = :email")
    List<Reservation> findReservationsByEmail(@Param("email") String paramString);

    @Query("select r from Reservation r where r.regDate = :searchDate")
    List<Reservation> findByDate(@Param("searchDate") LocalDate paramLocalDate);

    List<Reservation> findAllByRegDateBefore(LocalDate paramLocalDate);

    // 예약날짜별 조회
    @Query("SELECT SUM(r.member) FROM Reservation r WHERE r.regDate = :regDate AND r.state = true")
    Integer findConfirmedReservationsOnDate(@Param("regDate") LocalDate regDate);
}
