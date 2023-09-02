package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.MemberFormDto;
import com.haegreen.fishing.dto.ReservationDTO;
import com.haegreen.fishing.dto.ResponseDTO;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.entitiy.Reservation;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.repository.ReservationRepository;
import com.haegreen.fishing.security.CustomUserDetails;
import com.haegreen.fishing.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("reservation")
@Log4j2
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @GetMapping("list")
    public String list(Model model) {
        List<ReservationDTO> reservationDTOS = reservationService.getAllReservations();
        model.addAttribute("reservationDTOS", reservationDTOS);
        return "reservation/list";
    }

    @GetMapping("register")
    public String register(Model model, Authentication authentication, @RequestParam(value = "regDate") LocalDate regDate, MemberFormDto memberFormDto){

        List<Reservation> reservations = reservationRepository.findByDate(regDate);
        int extras = 16;
        for (Reservation reservation : reservations) {
            extras -= reservation.getMember();
        }
        if(extras <= 0) {
            extras = 16;
        }
        model.addAttribute("extras", extras);

        if (authentication != null && authentication.isAuthenticated()) {
            // 로그인한 사용자의 이름과 전화번호를 가져옵니다.
            // Member는 여기서 당신의 유저 클래스입니다.
            Member member = ((CustomUserDetails) authentication.getPrincipal()).getMember();
            memberFormDto.setName(member.getName());
            memberFormDto.setEmail(member.getEmail());
            // 전화번호 split
            String tel = member.getTel();
            String[] parts = tel.split("-");
            String tel1 = parts[0]; String tel2 = parts[1]; String tel3 = parts[2];
            memberFormDto.setTel1(tel1);
            memberFormDto.setTel2(tel2);
            memberFormDto.setTel3(tel3);

            model.addAttribute("memberFormDto", memberFormDto);
        } else {
            // 로그인하지 않은 경우 기본 값을 설정합니다.
            model.addAttribute("memberFormDto", memberFormDto);
        }

        return "reservation/register";
    }

    @PostMapping("register")
    public String register(ReservationDTO dto, RedirectAttributes redirectAttributes,
                           @RequestParam String tel1, @RequestParam String tel2, @RequestParam String tel3){

        String tel = tel1 + "-" + tel2 + "-" + tel3;
        dto.setTel(tel);

        Long rvno = reservationService.register(dto);

        redirectAttributes.addFlashAttribute("rvno", rvno);

        return "redirect:/reservation/success";
    }
    @GetMapping("success")
    public void success(Model model) {

    }

}
