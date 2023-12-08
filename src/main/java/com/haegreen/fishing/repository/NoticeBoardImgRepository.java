package com.haegreen.fishing.repository;

import com.haegreen.fishing.entitiy.NoticeBoard;
import com.haegreen.fishing.entitiy.NoticeBoardImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeBoardImgRepository extends JpaRepository<NoticeBoardImg, Long> {
    @Query("SELECT i from NoticeBoardImg i where i.noticeBoard = :nbno")
    List<NoticeBoardImg> GetImgbyNbno(@Param("nbno") NoticeBoard nbno);

    @Query("SELECT i from NoticeBoardImg i where i.noticeBoard.nbno = :nbno")
    List<NoticeBoardImg> findByNoticeBoardNbno(@Param("nbno") Long nbno);
}
