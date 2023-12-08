package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.ImgDTO;
import com.haegreen.fishing.dto.NoticeBoardDTO;
import com.haegreen.fishing.dto.PageRequestDTO;
import com.haegreen.fishing.dto.PageResultDTO;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.service.NoticeBoardImgService;
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
@RequestMapping("noticeboard")
@Log4j2
@RequiredArgsConstructor
public class NoticeBoardController {

    private final NoticeBoardService noticeBoardService;
    private final NoticeBoardImgService noticeBoardImgService;
    private final MemberRepository memberRepository;

    @GetMapping("list")
    public String list(PageRequestDTO pageRequestDTO, Model model){

        PageResultDTO<NoticeBoardDTO, Object[]> pageResultDTO = noticeBoardService.getList(pageRequestDTO);
        if(pageResultDTO.getTotalPage()==0){ pageResultDTO.setTotalPage(1);} // 글이 하나도 없을 땐 0으로 인식하므로

        pageRequestDTO.setType(pageRequestDTO.getTypeAsString());

        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("result", pageResultDTO);
        return "noticeboard/list";
    }

    @GetMapping("register")
    public String register(){
        return "noticeboard/register";
    }

    @PostMapping("register")
    public String registerPost(NoticeBoardDTO dto, RedirectAttributes redirectAttributes, Authentication authentication,
                               @RequestParam("images") List<MultipartFile> images){

        Member member = memberRepository.findByEmail(authentication.getName());

        dto.setWriterEmail(member.getEmail());

        Long nbno = noticeBoardService.register(dto); //새로 추가된 엔티티의 번호(dto)

        images.forEach(i -> {
            noticeBoardImgService.noticeBoardRegister(i, nbno);
        });

        redirectAttributes.addFlashAttribute("msg", "등록되었습니다.");

        return "redirect:/noticeboard/list";
    }

    @GetMapping({"read", "modify"})
    public void read(@ModelAttribute("requestDTO") PageRequestDTO pageRequestDTO, Long nbno, Model model){

        NoticeBoardDTO noticeBoardDTO = noticeBoardService.get(nbno);

        List<ImgDTO> noticeBoardImgDTOs = noticeBoardService.getImgList(nbno);

        model.addAttribute("noticeBoardImgs", noticeBoardImgDTOs);
        model.addAttribute("dto", noticeBoardDTO);
    }

    @PostMapping("remove")
    public String remove(Long nbno, RedirectAttributes redirectAttributes){

        noticeBoardService.removeWithReplies(nbno);
        noticeBoardImgService.remove(nbno);

        redirectAttributes.addFlashAttribute("msg", "삭제되었습니다.");

        return "redirect:/noticeboard/list";
    }

    @PostMapping("modify")
    public String modify(NoticeBoardDTO dto,
                         @ModelAttribute("requestDTO") PageRequestDTO requestDTO,
                         @RequestParam("images") List<MultipartFile> images,
                         @RequestParam(value = "inos", required = false) List<Long> ninoList,
                         @RequestParam(value = "deleteImages", required = false) List<Long> deleteImageIds,
                         RedirectAttributes redirectAttributes){

        if(ninoList == null || ninoList.isEmpty()){
            images.forEach(i -> {
                noticeBoardImgService.noticeBoardRegister(i, dto.getNbno());
            });
        }else {
            noticeBoardImgService.updateImages(ninoList, images);
        }

        if(deleteImageIds != null && !deleteImageIds.isEmpty()) {
            noticeBoardImgService.deleteImages(deleteImageIds);
        }

        noticeBoardService.modify(dto);

        redirectAttributes.addAttribute("page", requestDTO.getPage());
        redirectAttributes.addAttribute("type", requestDTO.getType());
        redirectAttributes.addAttribute("keyword", requestDTO.getKeyword());
        redirectAttributes.addAttribute("nbno", dto.getNbno());
        return "redirect:/noticeboard/read";
    }

}
