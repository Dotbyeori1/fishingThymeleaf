package com.haegreen.fishing.repository;

import com.haegreen.fishing.entitiy.NoticeBoard;
import com.haegreen.fishing.repository.search.SearchBoardRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface NoticeBoardRepository extends JpaRepository<NoticeBoard, Long>, QuerydslPredicateExecutor<NoticeBoard>, SearchBoardRepository {
    @Query("select b, w, count(r) from NoticeBoard b left join b.member w left outer join NoticeReply r on r.noticeBoard = b where b.nbno = :nbno")
    Object getNoticeBoardByNbno(@Param("nbno") Long nbno);

}
