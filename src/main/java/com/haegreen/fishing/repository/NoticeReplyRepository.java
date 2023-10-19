package com.haegreen.fishing.repository;

import com.haegreen.fishing.entitiy.NoticeBoard;
import com.haegreen.fishing.entitiy.NoticeReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeReplyRepository extends JpaRepository<NoticeReply, Long> {

    // 해당되는 게시물의 번호의 댓글을 삭제한다.
    @Modifying
    @Query("delete from NoticeReply r where r.noticeBoard.nbno =:nbno")
    void deleteByBno(@Param("nbno") Long nbno);

    //게시물로 댓글 목록 가져오기
    List<NoticeReply> getRepliesByNoticeBoardOrderByNrno(NoticeBoard noticeBoard);
}
