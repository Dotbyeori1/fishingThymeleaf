package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.*;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.service.JowhangBoardImgService;
import com.haegreen.fishing.service.JowhangBoardService;
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
@RequestMapping("jowhangboard")
@Log4j2
@RequiredArgsConstructor
public class JowhangBoardController {

    private final JowhangBoardService jowhangBoardService;
    private final JowhangBoardImgService jowhangBoardImgService;
    private final MemberRepository memberRepository;

    @GetMapping("list")
    public String list(PageRequestDTO pageRequestDTO, Model model){

        PageResultDTO<JowhangBoardDTO, Object[]> pageResultDTO = jowhangBoardService.getList(pageRequestDTO);
        if(pageResultDTO.getTotalPage()==0){ pageResultDTO.setTotalPage(1);} // 글이 하나도 없을 땐 0으로 인식하므로

        pageRequestDTO.setType(pageRequestDTO.getTypeAsString());

        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("result", pageResultDTO);

        return "jowhangboard/list";
    }

    @GetMapping("register")
    public String register(){
        return "jowhangboard/register";
    }

    @PostMapping("register")
    public String registerPost(JowhangBoardDTO dto, RedirectAttributes redirectAttributes, Authentication authentication,
                               @RequestParam("images") List<MultipartFile> images){

        Member member = memberRepository.findByEmail(authentication.getName());

        dto.setWriterEmail(member.getEmail());

        Long jbno = jowhangBoardService.register(dto); //새로 추가된 엔티티의 번호(dto)

        images.forEach(i -> {
            jowhangBoardImgService.jowhangBoardRegister(i, jbno);
        });

        redirectAttributes.addFlashAttribute("msg", "등록되었습니다.");

        return "redirect:/jowhangboard/list";
    }

    @GetMapping({"read", "modify"})
    public void read(@ModelAttribute("requestDTO") PageRequestDTO pageRequestDTO, Long jbno, Model model){

        JowhangBoardDTO jowhangBoardDTO = jowhangBoardService.get(jbno);

        List<ImgDTO>  jowhangBoardImgDTOs = jowhangBoardService.getImgList(jbno);

        model.addAttribute("jowhangBoardImgs", jowhangBoardImgDTOs);
        model.addAttribute("dto", jowhangBoardDTO);
    }

    @PostMapping("remove")
    public String remove(Long jbno, RedirectAttributes redirectAttributes){

        jowhangBoardService.removeWithReplies(jbno);
        jowhangBoardImgService.remove(jbno);

        redirectAttributes.addFlashAttribute("msg", "삭제되었습니다.");

        return "redirect:/jowhangboard/list";
    }

    @PostMapping("modify")
    public String modify(JowhangBoardDTO dto,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         @RequestParam("images") List<MultipartFile> images,
                         @RequestParam(value = "inos", required = false) List<Long> jinoList,
                         @RequestParam(value = "deleteImages", required = false) List<Long> deleteImageIds,
                         RedirectAttributes redirectAttributes){

        if(jinoList == null || jinoList.isEmpty()){
            images.forEach(i -> {
                jowhangBoardImgService.jowhangBoardRegister(i, dto.getJbno());
            });
        }else {
            jowhangBoardImgService.updateImages(jinoList, images);
        }

        if(deleteImageIds != null && !deleteImageIds.isEmpty()) {
            jowhangBoardImgService.deleteImages(deleteImageIds);
        }

        jowhangBoardService.modify(dto);

        redirectAttributes.addAttribute("page", requestDTO.getPage());
        redirectAttributes.addAttribute("type", requestDTO.getType());
        redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());
        redirectAttributes.addAttribute("jbno", dto.getJbno());
        return "redirect:/jowhangboard/read";
    }

}
