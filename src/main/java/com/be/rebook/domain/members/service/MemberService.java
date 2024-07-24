package com.be.rebook.domain.members.service;

import com.be.rebook.domain.members.dto.UpdateDTO;
import com.be.rebook.domain.members.entity.Members;
import com.be.rebook.domain.members.entity.RefreshTokens;
import com.be.rebook.domain.members.entity.Universities;
import com.be.rebook.domain.members.jwt.JWTUtil;
import com.be.rebook.domain.members.repository.MajorsRepository;
import com.be.rebook.domain.members.repository.MembersRepository;
import com.be.rebook.domain.members.repository.RefreshTokensRepository;
import com.be.rebook.domain.members.repository.UniversitiesRepository;
import com.be.rebook.global.config.BaseResponse;
import com.be.rebook.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MemberService {

    private final MembersRepository membersRepository;
    private final UniversitiesRepository universitiesRepository;
    private final MajorsRepository majorsRepository;
    private final JWTUtil jwtUtil;

    private static final Logger memberServiceLogger = LoggerFactory.getLogger(MemberService.class);
    private final RefreshTokensRepository refreshTokensRepository;

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

    private Boolean checkSpecialCharacters(String input){
        return input.matches(".*[^a-zA-Z0-9\\uAC00-\\uD7AF].*");
    }

    @Transactional
    public BaseResponse<Members> updateUser(String token, UpdateDTO membersUpdateDTO) {
        HttpStatus returnStatus = null;
        String returnCode = null;
        String returnMessage = null;

        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            //EXPIRED_TOKEN
            memberServiceLogger.error("회원 정보 업데이트 오류 : 토큰 만료됨 {}", ErrorCode.EXPIRED_TOKEN);
            returnStatus = ErrorCode.EXPIRED_TOKEN.getStatus();
            returnCode = returnStatus + " failed";
            returnMessage = ErrorCode.EXPIRED_TOKEN.getMessage();
            return new BaseResponse<>(returnStatus, returnCode, returnMessage, null);
        }

        String username = jwtUtil.getUsername(token);
        Boolean isUsernameExists = membersRepository.existsByUsername(username);

        if (Boolean.FALSE.equals(isUsernameExists)){
            //NO_USER_INFO
            memberServiceLogger.error("회원 정보 업데이트 오류 : 해당 유저 없음, 코드 {}", ErrorCode.NO_USER_INFO);
            returnStatus = ErrorCode.NO_USER_INFO.getStatus();
            returnCode = returnStatus + " failed";
            returnMessage = ErrorCode.NO_USER_INFO.getMessage();
            return new BaseResponse<>(returnStatus, returnCode, returnMessage, null);
        }

        Members member = membersRepository.findByUsername(username);
        String nickname = null;
        Long unvId = -1L;
        String majors = null;

        String nicknameToUpdate = membersUpdateDTO.getNickname();
        String unvToUpdate = membersUpdateDTO.getUniversity();

        if ((nicknameToUpdate != null && checkSpecialCharacters(nicknameToUpdate)) ||
                (unvToUpdate != null && checkSpecialCharacters(unvToUpdate))) {
            //BAD_INPUT
            memberServiceLogger.error("회원 정보 업데이트 오류 : 입력 형식 잘못됨, 코드 {}", ErrorCode.BAD_INPUT);
            returnStatus = ErrorCode.BAD_INPUT.getStatus();
            returnCode = returnStatus.toString() + " failed";
            returnMessage = ErrorCode.BAD_INPUT.getMessage();
            return new BaseResponse<>(returnStatus,returnCode,returnMessage,null);
        }

        if (nicknameToUpdate != null) {
            nickname = membersUpdateDTO.getNickname();
        }

        // string으로 들어온 학교를 universities 테이블에서 조회해서 id값 얻어오고
        // 그 아이디값 저장
        if (unvToUpdate != null) {
            unvId = universitiesRepository.findByUniversity(unvToUpdate).getUnvId();
        }

        // , 콤마로 전공명,전공명,전공명 이런식으로 들어온 데이터 ,로 나눠서 각 전공명별로 아이디값 조회해서
        // 멤버 majors 항목에 1,2,3,4,5 식의 스트링으로 저장하기
        String majorsToUpdate = membersUpdateDTO.getMajors();
        if (majorsToUpdate != null){
            if(majorsToUpdate.matches(".*[^a-zA-Z0-9,\\uAC00-\\uD7AF].*")){
                //BAD_INPUT
                memberServiceLogger.error("회원 정보 업데이트 오류 : 입력 형식 잘못됨, 코드 {}", ErrorCode.BAD_INPUT);
                returnStatus = ErrorCode.BAD_INPUT.getStatus();
                returnCode = returnStatus.toString() + " failed";
                returnMessage = ErrorCode.BAD_INPUT.getMessage();
                return new BaseResponse<>(returnStatus,returnCode,returnMessage,null);
            }

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
        Members updatedMember = member
                .toBuilder()
                .nickname(nickname)
                .university(unvId)
                .majors(majors)
                .build();

        membersRepository.save(updatedMember);
        return new BaseResponse<>(updatedMember);
    }

    // TroubleShooting
    //403 오류 해결
    // -> SecurityContextHolder에 토큰 생성할때 등록
    // -> isExpired에서 현재 날짜로 그 전에 토큰의 유효기간이 끝나는게 아니라 현재 날짜 + 유효기간으로 판단.
    public BaseResponse<Members> deleteUser(String token) {
        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            //EXPIRED_TOKEN
            memberServiceLogger.error("회원 탈퇴 오류 : 토큰 만료됨, 코드: {}", ErrorCode.EXPIRED_TOKEN);
            return new BaseResponse<>(ErrorCode.EXPIRED_TOKEN.getStatus(),
                    ErrorCode.EXPIRED_TOKEN.getStatus() + " failed",
                    ErrorCode.EXPIRED_TOKEN.getMessage(),
                    null);
        }

        String username = jwtUtil.getUsername(token);
        Boolean isUsernameExists = membersRepository.existsByUsername(username);

        if(Boolean.FALSE.equals(isUsernameExists)){
            //NO_USER_INFO
            memberServiceLogger.error("회원 탈퇴 오류 : 유저 없음, 코드: {}", ErrorCode.NO_USER_INFO);
            return new BaseResponse<>(ErrorCode.NO_USER_INFO.getStatus(),
                    ErrorCode.NO_USER_INFO.getStatus() + " failed",
                    ErrorCode.NO_TOKEN_CONTENT.getMessage(),
                    null);
        }

        Members member = membersRepository.findByUsername(username);
        List<RefreshTokens> refreshTokens = refreshTokensRepository.findByUsername(username);
        for(RefreshTokens tokenToDelete : refreshTokens){
            refreshTokensRepository.delete(tokenToDelete);
        }
        membersRepository.delete(member);
        return new BaseResponse<>(member);
    }

    public BaseResponse<List<String>> getUniversitiesList(String unvToSearch){
        if(unvToSearch.matches(".*[^가-힣\\sA-Z()].*")){
            //BAD_INPUT
            memberServiceLogger.error("검색어로 대학 목록 불러오기 오류 : 입력 형식 잘못됨, 코드: {}", ErrorCode.BAD_INPUT);
            return new BaseResponse<>(ErrorCode.BAD_INPUT.getStatus(),
                    ErrorCode.BAD_INPUT.getStatus() + " failed",
                    ErrorCode.BAD_INPUT.getMessage(),
                    null);
        }
        List<Universities> universitiesList = universitiesRepository.searchByUniversity(unvToSearch);
        List<String> returnList = new ArrayList<>();
        for(Universities unv : universitiesList){
            returnList.add(unv.getUniversity());
        }
        return new BaseResponse<>(returnList);
    }
}

