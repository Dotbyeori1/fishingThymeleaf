package com.haegreen.fishing.service;


import com.haegreen.fishing.dto.*;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.entitiy.NoticeBoard;
import com.haegreen.fishing.repository.MemberRepository;

import java.util.List;

public interface NoticeBoardService {

    Long register(NoticeBoardDTO dto); //등록
    NoticeBoardDTO get(Long bno); //조회
    void modify(NoticeBoardDTO noticeBoardDTO); //수정
    void removeWithReplies(Long bno); //삭제
    PageResultDTO<NoticeBoardDTO, Object[]> getList(PageRequestDTO pageRequestDTO); // 리스트 출력
    public List<ImgDTO> getImgList(Long bno);

    default NoticeBoard dtoToEntity(NoticeBoardDTO dto, MemberRepository memberRepository){

        Member member = memberRepository.findByEmail(dto.getWriterEmail());
        // Member객체 생성 - 리포지토리 실행결과를 담는다.
        // Member member = new Member(); X
        // 새 객체 생성은 당연히 새로운 행을 만든다는 소리니까! 참조가 안됨!

        NoticeBoard noticeBoard = NoticeBoard.builder()
                .nbno(dto.getNbno())
                .title(dto.getTitle())
                .content(dto.getContent())
                .member(member)
                .build();
        //상단에서 생성한 Member객체 활용 Board객체 생성

        return noticeBoard;
    }

    default NoticeBoardDTO entityToDTO(NoticeBoard noticeBoard, Member member, Long noticeReplyCount){

        NoticeBoardDTO noticeBoardDTO = NoticeBoardDTO.builder()
                .nbno(noticeBoard.getNbno())
                .title(noticeBoard.getTitle())
                .content(noticeBoard.getContent())
                .regTime(noticeBoard.getRegTime())
                .modTime(noticeBoard.getModTime())
                .createdBy(noticeBoard.getCreatedBy())
                .modifiedBy(noticeBoard.getModifiedBy())
                .writerEmail(member.getEmail())
                .writerName(member.getNickName())
                .replyCount(noticeReplyCount.intValue())
                .build();

        return noticeBoardDTO;
    }
}
