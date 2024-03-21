package com.haegreen.fishing.controller;

import com.haegreen.fishing.constant.Role;
import com.haegreen.fishing.dto.MemberFormDto;
import com.haegreen.fishing.dto.ReservationDTO;
import com.haegreen.fishing.dto.ReservationDateDTO;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.entitiy.Reservation;
import com.haegreen.fishing.entitiy.ReservationDate;
import com.haegreen.fishing.repository.ReservationDateRepository;
import com.haegreen.fishing.repository.ReservationRepository;
import com.haegreen.fishing.security.CustomUserDetails;
import com.haegreen.fishing.service.ReservationDateService;
import com.haegreen.fishing.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("reservation")
@Log4j2
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationDateService reservationDateService;
    private final ReservationRepository reservationRepository;
    private final ReservationDateRepository reservationDateRepository;

    private Boolean sendSms(String regName, LocalDate regDate, int mebmber) {
        String api_key = "NCS04BSHXUV9PONO";
        String api_secret = "I60QMWVQ0AZLLANTGFNHSQ64RI9EJERL";
        Message coolsms = new Message(api_key, api_secret);
        HashMap<String, String> params = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 ");
        String formattedDate = regDate.format(formatter);
        String text = regName + "님 " + formattedDate + mebmber + "명 예약이 등록되었습니다.";
        params.put("to", "010-4421-2628");
        params.put("from", "010-4421-2628");
        params.put("type", "SMS");
        params.put("text", text);
        params.put("app_version", "test app 1.2");
        try {
            JSONObject obj = coolsms.send(params);
            System.out.println(obj.toString());
            return Boolean.valueOf(true);
        } catch (CoolsmsException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
            return Boolean.valueOf(false);
        }
    }

    @GetMapping("list")
    public String list(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(1);
        LocalDate endDate = today.plusMonths(3);
        List<ReservationDTO> reservationDTOS = reservationService.getAllReservations(startDate, endDate);
        List<ReservationDateDTO> reservationDateDTOS = reservationDateService.getAllReservationDates(startDate, endDate);

        LocalTime currentTime = LocalTime.now();
        for(ReservationDateDTO reservationDateDTO : reservationDateDTOS){
            String fishingSort = reservationDateDTO.getFishingSort();
            if (fishingSort != null && ("갈치".contains(fishingSort)) && currentTime.isBefore(LocalTime.of(14, 0))) {
                reservationDateDTO.setAvailable(true);
            }
            if(!reservationDateDTO.isAvailable() || reservationDateDTO.isDateModify()){
                reservationDateDTO.setAvailable(false);
            }
        }
        model.addAttribute("reservationDTOS", reservationDTOS);
        model.addAttribute("reservationDateDTOS", reservationDateDTOS);
        return "reservation/list";
    }

    @GetMapping("register")
    public String register(Model model, Authentication authentication, RedirectAttributes redirectAttributes,
                           @RequestParam(value = "regDate") LocalDate regDate, MemberFormDto memberFormDto,
                           @RequestParam(value = "mainYN") Boolean mainYN) {

        ReservationDate reservationDate = reservationDateRepository.findReservationDateByRegDate(regDate);
        Integer confirmedMembers = reservationRepository.findConfirmedReservationsOnDate(regDate);

        int extras = reservationDate.getExtrasMembers() - (confirmedMembers != null ? confirmedMembers : 0);
        if (extras <= 0 || !reservationDateService.isReservable(reservationDate)) {
            redirectAttributes.addFlashAttribute("message", "잘못된접근");
            return "redirect:/reservation/list";
        }
        model.addAttribute("fishingMoney", reservationDate.getFishingMoney());
        model.addAttribute("extras", extras);
        model.addAttribute("mainYN", mainYN);

        // 인증이 확인되면 로그인한 사용자의 정보를 가져온다.
        if (authentication != null && authentication.isAuthenticated()) {
            Member member = ((CustomUserDetails) authentication.getPrincipal()).getMember();

            if (member.getRole() != Role.ADMIN) {
                // 관리자가 아닌 경우, 사용자 정보를 memberFormDto에 채운다.
                populateMemberFormDtoFromMember(memberFormDto, member);
            }
            model.addAttribute("memberFormDto", memberFormDto);

        } else {
            // 로그인하지 않은 경우, 기본 memberFormDto를 모델에 추가한다.
            model.addAttribute("memberFormDto", memberFormDto);
        }

        return "reservation/register";
    }

    // 메서드를 분리이용하여 재사용성을 높임
    private void populateMemberFormDtoFromMember(MemberFormDto memberFormDto, Member member) {
        memberFormDto.setName(member.getName());
        memberFormDto.setEmail(member.getEmail());

        // 전화번호를 분리한다.
        String[] parts = member.getTel().split("-");
        memberFormDto.setTel1(parts[0]);
        memberFormDto.setTel2(parts[1]);
        memberFormDto.setTel3(parts[2]);
    }


    @PostMapping("register")
    public String register(ReservationDTO dto, RedirectAttributes redirectAttributes, Authentication authentication) {

        String tel = dto.getTel1() + "-" + dto.getTel2() + "-" + dto.getTel3();
        dto.setTel(tel);

        if (dto.getMember() == 0) {
            redirectAttributes.addFlashAttribute("message", "0명 예약은 불가능합니다.");
            return "redirect:/reservation/list";
        }

        Long rvno = reservationService.register(dto);

        if (authentication != null && authentication.isAuthenticated()) {
            Member member = ((CustomUserDetails) authentication.getPrincipal()).getMember();
            if (member.getRole() != Role.ADMIN) {
                sendSms(dto.getRegName(), dto.getRegDate(), dto.getMember()); // 관리자가 아닌 사람이 등록시, 관리자에게 알림 문자 발송
            }
        } else {
            sendSms(dto.getRegName(), dto.getRegDate(), dto.getMember());
        }

        redirectAttributes.addFlashAttribute("rvno", rvno);

        return "redirect:/reservation/success";
    }

    @GetMapping("success")
    public void success(Model model) {

    }

    @GetMapping("check")
    public String check() { // 로그인 하지 않은 사람의 예약확인 페이지

        return "reservation/check";
    }

    @PostMapping("check")
    public String checkReservation(ReservationDTO reservationDTO, RedirectAttributes redirectAttributes,
                                   @RequestParam String tel1, @RequestParam String tel2, @RequestParam String tel3) { // 로그인 하지 않은 사람의 예약내역 조회

        String tel = tel1 + "-" + tel2 + "-" + tel3;
        reservationDTO.setTel(tel);
        Optional<Reservation> optionalReservation = reservationService.check(reservationDTO.getEmail(), reservationDTO.getRegName(), reservationDTO.getTel());

        if (optionalReservation.isPresent()) {
            redirectAttributes.addFlashAttribute("reservations", reservationDTO);
            return "redirect:/reservation/details";
        } else {
            // 실패 메시지 추가
            redirectAttributes.addFlashAttribute("message", "입력하신 정보로 조회된 예약 내역이 없습니다.");
            // 조회 페이지로 다시 리다이렉트
            return "redirect:/reservation/check";
        }
    }

    @GetMapping("details")
    public String reservationlist(@ModelAttribute("reservations") Optional<ReservationDTO> optionalReservationDTO, Authentication authentication, Model model) {
        List<ReservationDTO> reservationDTOS = new ArrayList<>();
        model.addAttribute("today", LocalDate.now());

        if (authentication != null && authentication.isAuthenticated()) {
            reservationDTOS = reservationService.getUserReservation(authentication, null);
        } else if (optionalReservationDTO.isPresent()) {
            ReservationDTO reservationDTO = optionalReservationDTO.get();
            reservationDTOS = reservationService.getUserReservation(null, reservationDTO);
        }

        if (reservationDTOS.isEmpty()) {
            model.addAttribute("message", "예약된 내역이 없습니다.");
        } else {
            model.addAttribute("reservationlist", reservationDTOS);
        }

        return "reservation/details";
    }


}
