package com.haegreen.fishing.service;

import com.haegreen.fishing.dto.MemberFormDto;
import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Log4j2
@Transactional
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private JavaMailSender emailSender; //이메일 전송해주는 기능을 DI받음

    // 비밀번호 암호화
    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    public Member saveMember(Member member) {
        vaildateDuplicateMember(member);
        return  memberRepository.save(member);
    }
    // 중복 회원 가입 막기
    private void vaildateDuplicateMember(Member member){
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    public Member editMember(MemberFormDto memberFormDto){
        Member member = memberRepository.findByEmail(memberFormDto.getEmail());
        member.setTel(memberFormDto.getTel());
        member.setName(memberFormDto.getName());
        member.setNickName(memberFormDto.getNickName());
        memberRepository.save(member);
        return member;
    }

    public Member changePassword(MemberFormDto memberFormDto){
        Member member = memberRepository.findByEmail(memberFormDto.getEmail());
        member.setPassword(passwordEncoder.encode(memberFormDto.getPassword()));
        memberRepository.save(member);

        return member;
    }

    // 비밀번호 찾기 메일
    public void sendEmail(String to, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage(); //메일 객체 생성
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message); //이메일 전송 기능을 통해서 위에서 생성한 메일 객체를 전송
    }

}
