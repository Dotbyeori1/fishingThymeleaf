package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.JowhangBoardDTO;
import com.haegreen.fishing.dto.JowhangReplyDTO;
import com.haegreen.fishing.entitiy.JowhangBoard;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.JowhangBoardRepository;
import com.haegreen.fishing.repository.JowhangReplyRepository;
import com.haegreen.fishing.security.CustomUserDetails;
import com.haegreen.fishing.service.JowhangBoardService;
import com.haegreen.fishing.service.JowhangReplyService;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("jowhangreply")
public class JowhangBoardReplyController {

    private final JowhangBoardService jowhangBoardService;
    private final JowhangBoardRepository jowhangBoardRepository;
    private final JowhangReplyService jowhangReplyService;
    private final JowhangReplyRepository jowhangReplyRepository;


    @PostMapping("register")
    public String register(JowhangBoardDTO jowhangBoardDTO, JowhangReplyDTO jowhangReplyDTO) throws NotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = ((CustomUserDetails) authentication.getPrincipal()).getMember();

        JowhangBoard jowhangBoard = jowhangBoardRepository.findById(jowhangBoardDTO.getJbno())
                .orElseThrow(() -> new NotFoundException("TourBoard not found"));

        jowhangReplyDTO.setReplyer(member.getEmail());
        jowhangReplyDTO.setJbno(jowhangBoard.getJbno());
        Long jrno = jowhangReplyService.register(jowhangReplyDTO);

        return "redirect:/jowhangboard/read?jbno="+jowhangBoard.getJbno();
    }

    @PostMapping("remove")
    public String remove(Long jrno, Long jbno, RedirectAttributes redirectAttributes) throws NotFoundException {

        jowhangReplyService.removeWithReplies(jrno, jbno);

        redirectAttributes.addFlashAttribute("msg", jrno);

        return "redirect:/jowhangboard/read?jbno="+jbno;
    }
}
