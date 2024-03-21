package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.*;
import com.haegreen.fishing.entitiy.*;
import com.haegreen.fishing.repository.*;
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
public class JowhangBoardServiceImpl implements JowhangBoardService{

    private final JowhangBoardRepository repository;
    private final JowhangReplyRepository jowhangReplyRepository;
    private final JowhangBoardImgRepository jowhangBoardImgRepository;
    private final MemberRepository memberRepository; // Member 객체를 불러오기 위해 필요

    @Override
    @Transactional
    public Long register(JowhangBoardDTO dto) {

        JowhangBoard jowhangBoard = dtoToEntity(dto, memberRepository);

        repository.save(jowhangBoard);

        return jowhangBoard.getJbno();
    }

    @Override
    public JowhangBoardDTO get(Long jbno) {
        Object result = repository.getJowhangBoardByJbno(jbno);

        Object[] arr = (Object[])result;

        return entityToDTO((JowhangBoard) arr[0], (Member) arr[1], (Long)arr[2]);
        // { {bno, writer, contet, category... }, {id, email,...}, {1}}
        //NoticeBoard, Member 엔티티와 댓글의 수(Long)를 가져오는 getBoardByBno메서드 이용 처리
    }


    @Override
    public PageResultDTO<JowhangBoardDTO, Object[]> getList(PageRequestDTO pageRequestDTO) {

        Function<Object[], JowhangBoardDTO> fn = (objectArr -> {
            JowhangBoard jowhangBoard = (JowhangBoard) objectArr[0];
            Member member = (Member) objectArr[1]; // 멤버 객체를 받아옵니다.
            Long count = (Long) objectArr[2]; // 카운트 정보를 받아옵니다.

            // 이미지 리스트를 가져옵니다.
            List<ImgDTO> imgDTOList = getImgList(jowhangBoard.getJbno());

            // DTO를 생성하고 값을 설정합니다.
            JowhangBoardDTO jowhangBoardDTO = entityToDTO(jowhangBoard, member, count);
            jowhangBoardDTO.setImgDTOList(imgDTOList);

            return jowhangBoardDTO;
        });

        //SearchBoardRepository에서 정의한 내용으로 세팅
        Page<Object[]> result; //Object결과를 Page객체에 담고
        String[] type = pageRequestDTO.getArrayType(); //검색 종류 담고
        Sort sort = Sort.by(Sort.Direction.DESC, "jbno"); //정렬방식 담음
        // SearchBaordRepositoryImpl에서 정의한 Sort 정렬방식과 맞춰야 한다
        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(), sort);

        //조회는 모든 사용자가 다 볼 수 있게 설정(Role에 상관없이)
        result = repository.jowhangBoardSearchList(type, pageRequestDTO.getKeyword(), pageable);

        return new PageResultDTO<>(result, fn);
    }

    @Transactional
    @Override
    public void modify(JowhangBoardDTO jowhangBoardDTO) {
        
        JowhangBoard jowhangBoard = repository.getOne(jowhangBoardDTO.getJbno()); //adminRepository에서 Board객체를 받아
        //필요한 순간까지 로딩을 지연하는 getOne메서드 이용

        jowhangBoard.changeTitle(jowhangBoardDTO.getTitle()); //Board객체의 제목 수정
        jowhangBoard.changeContent(jowhangBoardDTO.getContent()); //Board객체의 내용 수정

        repository.save(jowhangBoard); //수정된 객체를 repository에 저장
    }
    
    @Transactional
    @Override
    public void removeWithReplies(Long nbno) {

        jowhangReplyRepository.deleteByJbno(nbno); //댓글부터 삭제
        
        repository.deleteById(nbno); //이후 본 글 삭제
    }

    @Override
    public List<ImgDTO> getImgList(Long jbno) {
        List<ImgDTO> list = new ArrayList<>();
        JowhangBoard entity = repository.findById(jbno).get();
        jowhangBoardImgRepository.GetImgbyJbno(entity).forEach(i -> {
            ImgDTO imgDTO = new ImgDTO();
            imgDTO.setIno(i.getJino());
            imgDTO.setUuidfileName(i.getUuidfileName());
            imgDTO.setRealfileName(i.getRealfileName());
            imgDTO.setJowhangBoard(i.getJowhangBoard());
            list.add(imgDTO);
        });
        return list;
    }

}
