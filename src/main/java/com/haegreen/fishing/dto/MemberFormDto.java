package com.haegreen.fishing.dto;

import com.haegreen.fishing.constant.Role;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
public class MemberFormDto {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;

    @NotEmpty(message = "이메일은 필수 입력 값입니다.")
    private String email;

    private String password;

    @NotEmpty(message = "닉네임은 필수 입력 값입니다.")
    private String nickName; // 닉네임

    private String tel; // 전화번호
    private String tel1, tel2, tel3; // 전화번호 sprit

    private String id; // 아이디
    private String token; // 토큰
    private int point; // 포인트
    private Role role; // 역할
    private String authProvider; // 인증 제공자 (카카오톡, 깃허브)

    private boolean check15; // 자동로그인 체크 여부
}
