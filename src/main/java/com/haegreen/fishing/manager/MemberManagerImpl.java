package com.haegreen.fishing.manager;


import com.haegreen.fishing.entitiy.Member;
import com.haegreen.fishing.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

//authentication - > getname(); -> memberRepogirory -> entity
//하는 과정이 너무 많아서 통합해서 처리하는곳.
@RequiredArgsConstructor
@Service
public class MemberManagerImpl implements MemberManager{
    private final MemberRepository memberRepository;
    @Override
    public Member get(Authentication authentication) {
        return memberRepository.findByEmail(authentication.getName());
    }
}
