package com.haegreen.fishing.security;

import com.haegreen.fishing.service.CustomUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Log4j2
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String email = authToken.getPrincipal().getAttribute("email"); // DB에 있는지 email로 조회해야하니까 email을 불러온다

        // 소셜 로그인에 저장된 정보를 불러옴
        try {
            CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(email);

            SecurityContextHolder.getContext().setAuthentication // 스프링 시큐리티에도 로그인 사실 알려주기. 여러번 쓰지만 로그인한 정보를 담기위해 CustomUserDetails애 담아야함!
                    (new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities()));
            // "로그인" 클래스 불러오기 ( 로그인한유저정보 (member), Crendentials : password(소셜 로그인이라 비밀번호가 없어서 null), 권한)

            getRedirectStrategy().sendRedirect(request, response, "/?oauth=true");
        } catch (UsernameNotFoundException e) {
            // 소셜로그인 자체로는 스프링 시큐리티 로그인에 성공한 걸로 인식하기 때문에 로그아웃 처리를 해줘야됨.
            SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
            securityContextLogoutHandler.setInvalidateHttpSession(true); // 세션 무효화 처리
            securityContextLogoutHandler.logout(request, response, authentication); // 로그아웃 처리

            // 없으면 회원 가입 페이지로 리다이렉트 - session으로 데이터 줄려다가 안 돼서 빡쳐서 email을 파라미터로 보내기로 함
            getRedirectStrategy().sendRedirect(request, response, "/member/join?error=signup&email=" + email);
            return;
        }

        super.onAuthenticationSuccess(request, response, authentication); // 로그인 성공됐다고 알리기!
    }
}