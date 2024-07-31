package com.be.rebook.domain.members.repository;

import com.be.rebook.domain.members.entity.Members;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembersRepository extends JpaRepository<Members, Long> {
    Boolean existsByMemberId(Long memberId);

    Boolean existsByUsername(String username);

    Members findByUsername(String username);

    Members findByMemberId(Long memberId);
}