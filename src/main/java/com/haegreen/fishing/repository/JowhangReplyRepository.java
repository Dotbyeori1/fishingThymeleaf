package com.haegreen.fishing.repository;

import com.haegreen.fishing.entitiy.JowhangBoard;
import com.haegreen.fishing.entitiy.JowhangReply;
import com.haegreen.fishing.entitiy.NoticeBoard;
import com.haegreen.fishing.entitiy.NoticeReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JowhangReplyRepository extends JpaRepository<JowhangReply, Long> {

    // 해당되는 게시물의 번호의 댓글을 삭제한다.
    @Modifying
    @Query("delete from JowhangReply r where r.jowhangBoard.jbno =:jbno")
    void deleteByJbno(@Param("jbno") Long jbno);

    Page<JowhangReply> findByJowhangBoard_Jbno(Long jbno, Pageable pageable);

    //게시물로 댓글 목록 가져오기
    List<JowhangBoard> getRepliesByJowhangBoardOrderByJrno(JowhangBoard jowhangBoard);
}
