package com.be.rebook.domain.members.service;

import com.be.rebook.domain.members.dto.UpdateDTO;
import com.be.rebook.domain.members.entity.Members;
import com.be.rebook.domain.members.entity.RefreshTokens;
import com.be.rebook.domain.members.jwt.JWTUtil;
import com.be.rebook.domain.members.repository.MajorsRepository;
import com.be.rebook.domain.members.repository.MembersRepository;
import com.be.rebook.domain.members.repository.RefreshTokensRepository;
import com.be.rebook.domain.members.repository.UniversitiesRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    private MembersRepository membersRepository;
    private UniversitiesRepository universitiesRepository;
    private MajorsRepository majorsRepository;
    private JWTUtil jwtUtil;

    private RefreshTokensRepository refreshTokensRepository;

    public MemberService(MembersRepository membersRepository,
                         JWTUtil jwtUtil,
                         RefreshTokensRepository refreshTokensRepository,
                         UniversitiesRepository universitiesRepository,
                         MajorsRepository majorsRepository){
        this.membersRepository = membersRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokensRepository = refreshTokensRepository;
        this.universitiesRepository = universitiesRepository;
        this.majorsRepository = majorsRepository;
    }

    public Members getMemberByUsername(String username) {
        return membersRepository.findByUsername(username);
    }

    @Transactional
    public ResponseEntity<Members> updateUser(String token, UpdateDTO membersUpdateDTO) {
        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            System.out.println("회원 정보 업데이트 오류 : 토큰 만료됨");
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }

        String username = jwtUtil.getUsername(token);

        if (membersRepository.existsByUsername(username)) {
            Members member = membersRepository.findByUsername(username);
            String nickname = null;
            Long unvId = -1L;
            String majors = null;

            if (membersUpdateDTO.getNickname() != null) {
                nickname = membersUpdateDTO.getNickname();
            }

            // string으로 들어온 학교를 universities 테이블에서 조회해서 id값 얻어오고
            // 그 아이디값 저장
            if (membersUpdateDTO.getUniversity() != null) {
                String unv = membersUpdateDTO.getUniversity();
                unvId = universitiesRepository.findByUniversity(unv).getUnvId();
            }

            // , 콤마로 전공명,전공명,전공명 이런식으로 들어온 데이터 ,로 나눠서 각 전공명별로 아이디값 조회해서
            // 멤버 majors 항목에 1,2,3,4,5 식의 스트링으로 저장하기
            // -> 이상함 후에 어떻게 처리할지 생각해봐야됨 TODO
            if (membersUpdateDTO.getMajors() != null){
                String[] majorList = membersUpdateDTO.getMajors().split(",");
                StringBuilder sb = new StringBuilder();
                for(String major : majorList){
                    sb.append(majorsRepository.findByMajor(major).getMajorId());
                    sb.append(",");
                }
                majors = sb.toString();
            }

            //문제 : builder 패턴 써서 특정 레코드 업데이트하는 방법?
            //해결 -> toBuilder true 썼음
            //member에 toBuilder 접근하면 그 결과 다시 저장해줘야함
            //문제 : 자꾸 수정하면 거기다가 수정하는게 아니라 새 데이터가 생성되어버림
            //해결 -> 빌더 어노테이션 설정한 생성자에 자동으로 증가하는 id값을 쓰더라도 생성자 초기화에서 id값을 빼면 안됨
            Members updatedMember = member.toBuilder().nickname(nickname).university(unvId).majors(majors).build();

            membersRepository.save(updatedMember);
            return ResponseEntity.ok(updatedMember);
        } else {
            System.out.println("회원 정보 업데이트 오류 : 해당 유저 없음");
            return ResponseEntity.notFound().build();
        }
    }

    // TroubleShooting
    //403 오류 해결
    // -> SecurityContextHolder에 토큰 생성할때 등록
    // -> isExpired에서 현재 날짜로 그 전에 토큰의 유효기간이 끝나는게 아니라 현재 날짜 + 유효기간으로 판단.
    public ResponseEntity<Void> deleteUser(String token) {
        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            System.out.println("회원 탈퇴 오류 : 토큰 만료됨");
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }

        String username = jwtUtil.getUsername(token);

        if (membersRepository.existsByUsername(username)) {
            Members member = membersRepository.findByUsername(username);
            List<RefreshTokens> refreshTokens = refreshTokensRepository.findByUsername(username);
            for(RefreshTokens tokenToDelete : refreshTokens){
                refreshTokensRepository.delete(tokenToDelete);
            }
            membersRepository.delete(member);
            return ResponseEntity.ok().build();
        } else {
            System.out.println("회원 탈퇴 오류 : 유저 없음 ");
            return ResponseEntity.notFound().build();
        }
    }
}

