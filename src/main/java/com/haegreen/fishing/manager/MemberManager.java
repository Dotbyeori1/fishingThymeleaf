package com.haegreen.fishing.manager;

import com.haegreen.fishing.entitiy.Member;
import org.springframework.security.core.Authentication;

public interface MemberManager {
    Member get(Authentication authentication);
}
