package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.ImgDTO;
import com.haegreen.fishing.dto.NoticeBoardDTO;
import com.haegreen.fishing.dto.PageRequestDTO;
import com.haegreen.fishing.dto.PageResultDTO;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.service.ImgService;
import com.haegreen.fishing.service.NoticeBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("adminboard")
@Log4j2
@RequiredArgsConstructor
public class NoticeBoardController {

    private final NoticeBoardService noticeBoardService;
    private final ImgService imgService;
    private final MemberRepository memberRepository;

    @GetMapping("/list")
    public String list(PageRequestDTO pageRequestDTO, Model model){

        PageResultDTO<NoticeBoardDTO, Object[]> pageResultDTO = noticeBoardService.getList(pageRequestDTO);
        if(pageResultDTO.getTotalPage()==0){ pageResultDTO.setTotalPage(1);} // 글이 하나도 없을 땐 0으로 인식하므로

        model.addAttribute("result", pageResultDTO);
        return "noticeboard/list";
    }

    @GetMapping("register")
    public String register( Model model){
        return "noticeboard/register";
    }

    @PostMapping("register")
    public String registerPost(NoticeBoardDTO dto, RedirectAttributes redirectAttributes, Authentication authentication,
                               @RequestParam("images") List<MultipartFile> images){

        Member member = memberRepository.findByEmail(authentication.getName());

        dto.setWriterEmail(member.getEmail());

        Long nbno = noticeBoardService.register(dto); //새로 추가된 엔티티의 번호(dto)

        images.forEach(i -> {
            imgService.noticeBoardRegister(i, nbno);
        });

        redirectAttributes.addFlashAttribute("msg", nbno);

        return "redirect:/adminboard/"+dto.getCategory();
    }

    @GetMapping({"read", "modify"})
    public void read(@ModelAttribute("requestDTO") PageRequestDTO pageRequestDTO, Long bno, Model model){
        log.info("bno : " + bno);

        NoticeBoardDTO adminBoardDTO = noticeBoardService.get(bno);

        log.info(adminBoardDTO);

        List<ImgDTO> noticeBoardImgDTOs = noticeBoardService.getImgList(bno);

        System.out.println("image : " + noticeBoardImgDTOs);

        model.addAttribute("adminBoardImgs", noticeBoardImgDTOs);
        model.addAttribute("dto", adminBoardDTO);
    }

    @PostMapping("remove")
    public String remove(long nbno, RedirectAttributes redirectAttributes){
        log.info("bno : " + nbno);

        noticeBoardService.removeWithReplies(nbno);

        redirectAttributes.addFlashAttribute("msg", nbno);

        return "redirect:/adminboard/list";
    }

    @PostMapping("modify")
    public String modify(NoticeBoardDTO dto, @ModelAttribute("requestDTO") PageRequestDTO requestDTO, RedirectAttributes redirectAttributes){
        log.info("post modify.....");
        log.info("dto : " + dto);

        noticeBoardService.modify(dto);

        redirectAttributes.addAttribute("page", requestDTO.getPage());
        redirectAttributes.addAttribute("type", requestDTO.getType());
        redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());
        redirectAttributes.addAttribute("bno", dto.getNbno());
        return "redirect:/adminboard/read";
    }

}
