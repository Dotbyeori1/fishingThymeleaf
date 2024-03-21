package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.*;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;
import com.haegreen.fishing.security.CustomUserDetails;
import com.haegreen.fishing.security.TokenProvider;
import com.haegreen.fishing.service.JowhangBoardService;
import com.haegreen.fishing.service.ReservationDateService;
import com.haegreen.fishing.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MainController {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private final ReservationService reservationService;
    private final ReservationDateService reservationDateService;
    private final JowhangBoardService jowhangBoardService;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    @GetMapping("")
    public String intro() {
        return "intro";
    }

    @PostMapping("/oauth")
    public ResponseEntity<?> oauthSuccess (@RequestParam(value = "oauth", required = false) String oauth){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MemberFormDto memberFormDto = new MemberFormDto();
        if (principal instanceof CustomUserDetails && Objects.equals(oauth, "true")) {
            Member member = ((CustomUserDetails) principal).getMember();
            String token = tokenProvider.create((CustomUserDetails) principal, secretKey);
            memberFormDto.setRole(member.getRole());
            memberFormDto.setToken(token);
            String refreshToken = tokenProvider.createRefreshToken((CustomUserDetails) principal, secretKey);
            member.setRefreshToken(refreshToken);
            memberRepository.save(member);
        }
        return ResponseEntity.ok(memberFormDto);
    }

    @GetMapping(value = "main")
    public String main(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(9);
        List<ReservationDTO> reservationDTOS = reservationService.getAllReservations(today, endDate);
        List<ReservationDateDTO> reservationDateDTOS = reservationDateService.getAllReservationDates(today, endDate);

        // 갈치 시간 바꿈
        LocalTime currentTime = LocalTime.now();
        for(ReservationDateDTO reservationDateDTO : reservationDateDTOS){
            String fishingSort = reservationDateDTO.getFishingSort();
            if (fishingSort != null && ("갈치".contains(fishingSort)) && currentTime.isBefore(LocalTime.of(14, 0))) {
                reservationDateDTO.setAvailable(true);
            }else if(!reservationDateDTO.isAvailable() || reservationDateDTO.isDateModify()){
                reservationDateDTO.setAvailable(false);
            }
        }
        model.addAttribute("reservationDTOS", reservationDTOS);
        model.addAttribute("reservationDateDTOS", reservationDateDTOS);

        int page = 1;
        int pageSize = 10;
        PageRequestDTO pageRequestDTO = new PageRequestDTO(page, pageSize);
        PageResultDTO<JowhangBoardDTO, Object[]> jowhangBoard = jowhangBoardService.getList(pageRequestDTO);

        model.addAttribute("jowhangBoard", jowhangBoard);

        return "main";
    }

    @PostMapping(value = "auth")
    private ResponseEntity<?> auth(MemberFormDto memberFormDto, Authentication authentication,
                                   HttpServletRequest request, HttpServletResponse response) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = null;
        if (principal instanceof CustomUserDetails) {
            member = ((CustomUserDetails) principal).getMember();

            if (tokenProvider.isTokenExpired(member.getRefreshToken(), secretKey)) {
                ResponseDTO responseDTO = new ResponseDTO();
                responseDTO.setError("재로그인이 필요합니다.");
                SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
                securityContextLogoutHandler.setInvalidateHttpSession(true); // 세션 무효화 처리
                securityContextLogoutHandler.logout(request, response, authentication); // 로그아웃 처리
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
            }

            String token = tokenProvider.create((CustomUserDetails) principal, secretKey);
            memberFormDto.setToken(token);
        } else {
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setError("인증되지 않은 사용자 또는 세션이 만료되었습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
        }
        return ResponseEntity.ok(memberFormDto);
    }
}
