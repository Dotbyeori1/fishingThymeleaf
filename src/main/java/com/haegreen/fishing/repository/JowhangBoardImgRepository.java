package com.haegreen.fishing.repository;

import com.haegreen.fishing.entitiy.JowhangBoard;
import com.haegreen.fishing.entitiy.JowhangBoardImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JowhangBoardImgRepository extends JpaRepository<JowhangBoardImg, Long> {
    @Query("SELECT i from JowhangBoardImg i where i.jowhangBoard = :jbno")
    List<JowhangBoardImg> GetImgbyJbno(@Param("jbno") JowhangBoard jbno);

    @Query("SELECT i from JowhangBoardImg i where i.jowhangBoard.jbno = :jbno")
    List<JowhangBoardImg> findByJowhangBoardJbno(@Param("jbno") Long jbno);
}
