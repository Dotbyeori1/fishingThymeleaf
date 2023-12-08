package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.JowhangReplyDTO;
import com.haegreen.fishing.dto.PageRequestDTO;
import com.haegreen.fishing.dto.PageResultDTO;
import com.haegreen.fishing.entitiy.JowhangBoard;
import com.haegreen.fishing.entitiy.JowhangReply;
import com.haegreen.fishing.entitiy.Member;
import javassist.NotFoundException;

public interface JowhangReplyService {


    Long register(JowhangReplyDTO dto) throws NotFoundException; //등록

    JowhangReplyDTO get(Long jbno); //조회

    void modify(JowhangReplyDTO jowhangReplyDTO); //수정

    public void removeWithReplies(Long tbrno, Long tbno) throws NotFoundException; //삭제

    public PageResultDTO<JowhangReplyDTO, JowhangReply> getJowhangReplysAndPageInfoByJowhangBoardId(Long jrno, PageRequestDTO pageRequestDTO); // 리스트 출력

    
    // DTO 객체를 Entity 객체로 변환하는 메소드
    default JowhangReply dtoToEntity(JowhangReplyDTO dto, JowhangBoard jowhangBoard, Member member) {

        return JowhangReply.builder()
                .jrno(dto.getJrno())
                .text(dto.getText())
                .member(member)
                .jowhangBoard(jowhangBoard)
                .build();
    }

    // Entity 객체를 DTO 객체로 변환하는 메소드
    default JowhangReplyDTO entityToDTO(JowhangReply jowhangReply, Member member) {

        return JowhangReplyDTO.builder()
                .jrno(jowhangReply.getJrno())
                .text(jowhangReply.getText())
                .replyer(member.getNickName())
                .regDate(jowhangReply.getRegTime())
                .modDate(jowhangReply.getModTime())
                .build();
    }
}
