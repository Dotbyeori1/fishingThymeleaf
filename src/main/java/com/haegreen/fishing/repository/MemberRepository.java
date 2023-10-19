package com.haegreen.fishing.repository;

import com.haegreen.fishing.entitiy.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    Member findByEmail(String email);

    Boolean existsByEmail (String email);

}
