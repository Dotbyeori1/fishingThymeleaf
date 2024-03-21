package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.ImgDTO;
import com.haegreen.fishing.dto.NoticeBoardDTO;
import com.haegreen.fishing.dto.PageRequestDTO;
import com.haegreen.fishing.dto.PageResultDTO;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.entitiy.NoticeBoard;
import com.haegreen.fishing.entitiy.NoticeBoardImg;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.repository.NoticeBoardImgRepository;
import com.haegreen.fishing.repository.NoticeBoardRepository;
import com.haegreen.fishing.repository.NoticeReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class NoticeBoardServiceImpl implements NoticeBoardService{

    private final NoticeBoardRepository repository;
    private final NoticeReplyRepository noticeReplyRepository;
    private final NoticeBoardImgRepository noticeBoardImgRepository;
    private final MemberRepository memberRepository; // Member 객체를 불러오기 위해 필요

    @Override
    @Transactional
    public Long register(NoticeBoardDTO dto) {

        NoticeBoard noticeBoard = dtoToEntity(dto, memberRepository);

        repository.save(noticeBoard);

        return noticeBoard.getNbno();
    }

    @Override
    public NoticeBoardDTO get(Long nbno) {
        Object result = repository.getNoticeBoardByNbno(nbno);

        Object[] arr = (Object[])result;

        return entityToDTO((NoticeBoard) arr[0], (Member) arr[1], (Long)arr[2]);
        // { {bno, writer, contet, category... }, {id, email,...}, {1}}
        //NoticeBoard, Member 엔티티와 댓글의 수(Long)를 가져오는 getBoardByBno메서드 이용 처리
    }


    @Override
    public PageResultDTO<NoticeBoardDTO, Object[]> getList(PageRequestDTO pageRequestDTO) {

        Function<Object[], NoticeBoardDTO> fn = (en -> entityToDTO((NoticeBoard) en[0], (Member)en[1], (Long)en[2]));

        //SearchBoardRepository에서 정의한 내용으로 세팅
        Page<Object[]> result; //Object결과를 Page객체에 담고
        String[] type = pageRequestDTO.getArrayType(); //검색 종류 담고
        Sort sort = Sort.by(Sort.Direction.DESC, "nbno"); //정렬방식 담음
        // SearchBaordRepositoryImpl에서 정의한 Sort 정렬방식과 맞춰야 한다
        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(), sort);

        //조회는 모든 사용자가 다 볼 수 있게 설정(Role에 상관없이)
        result = repository.noticeBoardSearchList(type, pageRequestDTO.getKeyword(), pageable);

        return new PageResultDTO<>(result, fn);
    }

    @Transactional
    @Override
    public void modify(NoticeBoardDTO noticeBoardDTO) {
        
        NoticeBoard noticeBoard = repository.getOne(noticeBoardDTO.getNbno()); //adminRepository에서 Board객체를 받아
        //필요한 순간까지 로딩을 지연하는 getOne메서드 이용

        noticeBoard.changeTitle(noticeBoardDTO.getTitle()); //Board객체의 제목 수정
        noticeBoard.changeContent(noticeBoardDTO.getContent()); //Board객체의 내용 수정

        repository.save(noticeBoard); //수정된 객체를 repository에 저장
    }
    
    @Transactional
    @Override
    public void removeWithReplies(Long nbno) {

        noticeReplyRepository.deleteByBno(nbno); //댓글부터 삭제
        repository.deleteById(nbno); //이후 본 글 삭제
    }

    @Override
    public List<ImgDTO> getImgList(Long nbno) {
        List<ImgDTO> list = new ArrayList<>();
        NoticeBoard entity = repository.findById(nbno).get();
        noticeBoardImgRepository.GetImgbyNbno(entity).forEach(i -> {
            ImgDTO imgDTO = new ImgDTO();
            imgDTO.setIno(i.getNino());
            imgDTO.setUuidfileName(i.getUuidfileName());
            imgDTO.setRealfileName(i.getRealfileName());
            imgDTO.setNoticeBoard(i.getNoticeBoard());
            list.add(imgDTO);
        });
        return list;
    }

}
