package com.be.rebook.domain.members.repository;

import com.be.rebook.domain.members.entity.Majors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface MajorsRepository extends JpaRepository<Majors, Long> {
    @Query("SELECT m FROM Majors m WHERE m.major LIKE %:majorToSearch%")
    List<Majors> searchByMajor(@Param("majorToSearch") String majorToSearch);

    Majors findByMajor(String major);

    Majors findByMajorId(String majorId);
}
