package com.be.rebook.domain.members.repository;

import com.be.rebook.domain.members.entity.Majors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface MajorsRepository extends JpaRepository<Majors, Long> {
    // 프론트에서 전공명 입력할때 해당 문자열이 들어가는 모든 전공명들 죄다 출력할때
    @Query("SELECT m FROM Majors m WHERE m.major LIKE %:majorToSearch%")
    List<Majors> searchByMajor(@Param("majorToSearch") String majorToSearch);

    // 전공명으로 id 받아오기
    Majors findByMajor(String major);

    // id값으로 전공명 조회하기
    Majors findByMajorId(String majorId);
}
