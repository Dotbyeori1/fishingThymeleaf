package com.haegreen.fishing.repository;

import com.haegreen.fishing.entitiy.JowhangBoard;
import com.haegreen.fishing.repository.search.SearchBoardRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface JowhangBoardRepository extends JpaRepository<JowhangBoard, Long>, QuerydslPredicateExecutor<JowhangBoard>, SearchBoardRepository {
    @Query("select j, w, count(r) from JowhangBoard j left join j.member w left outer join JowhangReply r on r.jowhangBoard = j where j.jbno = :jbno")
    Object getJowhangBoardByJbno(@Param("jbno") Long jbno);

}
