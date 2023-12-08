package com.haegreen.fishing.service;


import com.haegreen.fishing.dto.ImgDTO;
import com.haegreen.fishing.dto.JowhangBoardDTO;
import com.haegreen.fishing.dto.PageRequestDTO;
import com.haegreen.fishing.dto.PageResultDTO;
import com.haegreen.fishing.entitiy.JowhangBoard;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;

import java.util.List;

public interface JowhangBoardService {

    Long register(JowhangBoardDTO dto); //등록
    JowhangBoardDTO get(Long jbno); //조회
    void modify(JowhangBoardDTO jowhangBoardDTO); //수정
    void removeWithReplies(Long jbno); //삭제
    PageResultDTO<JowhangBoardDTO, Object[]> getList(PageRequestDTO pageRequestDTO); // 리스트 출력
    public List<ImgDTO> getImgList(Long jbno);

    default JowhangBoard dtoToEntity(JowhangBoardDTO dto, MemberRepository memberRepository){

        Member member = memberRepository.findByEmail(dto.getWriterEmail());
        // Member객체 생성 - 리포지토리 실행결과를 담는다.
        // Member member = new Member(); X
        // 새 객체 생성은 당연히 새로운 행을 만든다는 소리니까! 참조가 안됨!

        JowhangBoard jowhangBoard = JowhangBoard.builder()
                .jbno(dto.getJbno())
                .title(dto.getTitle())
                .content(dto.getContent())
                .member(member)
                .build();
        //상단에서 생성한 Member객체 활용 Board객체 생성

        return jowhangBoard;
    }

    default JowhangBoardDTO entityToDTO(JowhangBoard jowhangBoard, Member member, Long noticeReplyCount){

        JowhangBoardDTO jowhangBoardDTO = JowhangBoardDTO.builder()
                .jbno(jowhangBoard.getJbno())
                .title(jowhangBoard.getTitle())
                .content(jowhangBoard.getContent())
                .regTime(jowhangBoard.getRegTime())
                .modTime(jowhangBoard.getModTime())
                .createdBy(jowhangBoard.getCreatedBy())
                .modifiedBy(jowhangBoard.getModifiedBy())
                .writerEmail(member.getEmail())
                .writerName(member.getNickName())
                .replyCount(noticeReplyCount.intValue())
                .build();

        return jowhangBoardDTO;
    }
}
