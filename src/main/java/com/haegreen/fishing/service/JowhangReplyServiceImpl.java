package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.JowhangReplyDTO;
import com.haegreen.fishing.dto.PageRequestDTO;
import com.haegreen.fishing.dto.PageResultDTO;
import com.haegreen.fishing.entitiy.JowhangBoard;
import com.haegreen.fishing.entitiy.JowhangReply;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.JowhangBoardRepository;
import com.haegreen.fishing.repository.JowhangReplyRepository;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.security.CustomUserDetails;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Log4j2
public class JowhangReplyServiceImpl implements JowhangReplyService {

    private final JowhangReplyRepository jowhangReplyRepository;
    private final JowhangBoardRepository jowhangBoardRepository;
    private final MemberRepository memberRepository;

    @Override
    public PageResultDTO<JowhangReplyDTO, JowhangReply> getJowhangReplysAndPageInfoByJowhangBoardId(Long jbno, PageRequestDTO pageRequestDTO) {
        Sort sort = Sort.by(Sort.Direction.DESC, "tbrno");

        Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(), sort);

        Page<JowhangReply> result = jowhangReplyRepository.findByJowhangBoard_Jbno(jbno, pageable);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member authMember;
        if (principal instanceof CustomUserDetails) {
            authMember = ((CustomUserDetails) principal).getMember();
        } else {
            authMember = null;
        }

        Function<JowhangReply, JowhangReplyDTO> fn = (jowhangReply -> {
            Member member = jowhangReply.getMember();
            JowhangReplyDTO jowhangReplyDTO = entityToDTO(jowhangReply, member);

            if (authMember != null && authMember.getId().equals(member.getId())) {
                jowhangReplyDTO.setMemberState(true);
            } else {
                jowhangReplyDTO.setMemberState(false);
            }

            return jowhangReplyDTO;
        });

        return new PageResultDTO<>(result, fn);

    }



    @Transactional
    @Override
    public Long register(JowhangReplyDTO dto) throws NotFoundException {
        Optional<Member> memberOptional = Optional.ofNullable(memberRepository.findByEmail(dto.getReplyer()));
        if (!memberOptional.isPresent()) {
            throw new NotFoundException("Member not found");
        }
        Member member = memberOptional.get();

        Optional<JowhangBoard> tourBoardOptional = jowhangBoardRepository.findById(dto.getJbno());
        if (!tourBoardOptional.isPresent()) {
            throw new NotFoundException("JowhangBoard not found");
        }
        JowhangBoard jowhangBoard = tourBoardOptional.get();

        int currentCount = jowhangBoard.getReplyCount();

        jowhangBoard.setReplyCount(currentCount + 1);
        jowhangBoardRepository.saveAndFlush(jowhangBoard);

        JowhangReply jowhangReply = dtoToEntity(dto, jowhangBoard, member);
        jowhangReplyRepository.save(jowhangReply);
        return jowhangReply.getJrno();
    }

    @Override
    public JowhangReplyDTO get(Long bno) {
        return null;
    }

    @Override
    public void modify(JowhangReplyDTO jowhangReplyDTO) {

    }

    @Override
    @Transactional
    public void removeWithReplies(Long jrno, Long jbno) throws NotFoundException {

        // 게시물 조회
        JowhangBoard jowhangBoard = jowhangBoardRepository.findById(jbno)
                .orElseThrow(() -> new NotFoundException("jowhangBoard not found"));

        // 댓글 삭제
        jowhangReplyRepository.deleteById(jrno);

        // 댓글 수 업데이트
        int currentCount = jowhangBoard.getReplyCount();
        if (currentCount > 0) {
            jowhangBoard.setReplyCount(currentCount - 1);
        } else {
            jowhangBoard.setReplyCount(0);
        }

        jowhangBoardRepository.save(jowhangBoard);
    }

}


