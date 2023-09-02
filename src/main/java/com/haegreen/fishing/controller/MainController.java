package com.haegreen.fishing.controller;

import com.haegreen.fishing.dto.MemberFormDto;
import com.haegreen.fishing.dto.ResponseDTO;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.security.CustomUserDetails;
import com.haegreen.fishing.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MainController {

    private final TokenProvider tokenProvider;

    @GetMapping("")
    public String intro(Model model){

        return "intro";
    }

    @GetMapping(value = "main")
    public String main() {

        return "main";
    }

    @GetMapping(value = "auth")
    private ResponseEntity<?> auth(MemberFormDto memberFormDto, Authentication authentication,
                                   HttpServletRequest request, HttpServletResponse response) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member member = null;
        if (principal instanceof CustomUserDetails) {
            member = ((CustomUserDetails) principal).getMember();

            if (tokenProvider.isTokenExpired(member.getRefreshToken())) {
                ResponseDTO responseDTO = new ResponseDTO();
                responseDTO.setError("재로그인이 필요합니다.");
                SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
                securityContextLogoutHandler.setInvalidateHttpSession(true); // 세션 무효화 처리
                securityContextLogoutHandler.logout(request, response, authentication); // 로그아웃 처리
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
            }

            String token = tokenProvider.create((UserDetails) principal);
            memberFormDto.setToken(token);
        }else {
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setError("인증되지 않은 사용자 또는 세션이 만료되었습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
        }
        return ResponseEntity.ok(memberFormDto);
    }
}
