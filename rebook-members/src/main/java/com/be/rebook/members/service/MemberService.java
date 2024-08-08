package com.be.rebook.members.service;

import com.be.rebook.members.entity.Majors;
import com.be.rebook.members.entity.Universities;
import com.be.rebook.members.repository.MajorsRepository;
import com.be.rebook.members.repository.MembersRepository;
import com.be.rebook.members.repository.UniversitiesRepository;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


// TODO: 시큐리티 로직 auth 서버에 의존하도록 리팩토링
@Service
public class MemberService {

    private final MembersRepository membersRepository;
    private final UniversitiesRepository universitiesRepository;
    private final MajorsRepository majorsRepository;
    // private final JWTUtil jwtUtil;
    // private final RefreshTokensRepository refreshTokensRepository;

    public MemberService(MembersRepository membersRepository,
                        //  JWTUtil jwtUtil,
                        //  RefreshTokensRepository refreshTokensRepository,
                         UniversitiesRepository universitiesRepository,
                         MajorsRepository majorsRepository){
        this.membersRepository = membersRepository;
        // this.jwtUtil = jwtUtil;
        // this.refreshTokensRepository = refreshTokensRepository;
        this.universitiesRepository = universitiesRepository;
        this.majorsRepository = majorsRepository;
    }

    private Boolean checkSpecialCharacters(String input){
        return input.matches(".*[^가-힣a-zA-Z0-9].*");
    }

    // @Transactional
    // public Members updateUser(String token, UpdateDTO membersUpdateDTO) {
    //     if(Boolean.TRUE.equals(jwtUtil.isExpired(token))){
    //         //EXPIRED_TOKEN
    //         throw new BaseException(ErrorCode.EXPIRED_TOKEN);
    //     }

    //     String username = jwtUtil.getUsername(token);
    //     Members member = membersRepository.findByUsername(username)
    //             .orElseThrow(()->new BaseException(ErrorCode.NO_USER_INFO));
    //     String nickname = null;
    //     Long unvId = -1L;
    //     String majors = null;

    //     String nicknameToUpdate = membersUpdateDTO.getNickname();
    //     String unvToUpdate = membersUpdateDTO.getUniversity();

    //     if (nicknameToUpdate != null) {
    //         nickname = membersUpdateDTO.getNickname();
    //     }

    //     if (unvToUpdate != null) {
    //         unvId = universitiesRepository
    //                 .findByUniversity(unvToUpdate)
    //                 .orElseThrow(()->new BaseException(ErrorCode.NO_UNIVERSITY_INFO))
    //                 .getUnvId();
    //     }

    //     String majorsToUpdate = membersUpdateDTO.getMajors();
    //     if (majorsToUpdate != null){
    //         if(majorsToUpdate.matches(".*[^a-zA-Z0-9,\\uAC00-\\uD7AF\\u4E00-\\u9FFF ()\\u00B7-].*")){
    //             //BAD_INPUT
    //             throw new BaseException(ErrorCode.BAD_INPUT);
    //         }

    //         String[] majorList = membersUpdateDTO.getMajors().split(",");
    //         StringBuilder sb = new StringBuilder();
    //         for(String major : majorList){
    //             sb.append(majorsRepository
    //                     .findByMajor(major)
    //                     .orElseThrow(()->new BaseException(ErrorCode.NO_MAJOR_INFO))
    //                     .getMajorId());
    //             sb.append(",");
    //         }
    //         majors = sb.toString();
    //     }

    //     Members updatedMember = member
    //             .toBuilder()
    //             .nickname(nickname)
    //             .university(unvId)
    //             .majors(majors)
    //             .build();

    //     membersRepository.save(updatedMember);
    //     return updatedMember;
    // }

    // public Members deleteUser(String token) {
    //     if(Boolean.TRUE.equals(jwtUtil.isExpired(token))){
    //         //EXPIRED_TOKEN
    //         throw new BaseException(ErrorCode.EXPIRED_TOKEN);
    //     }

    //     String username = jwtUtil.getUsername(token);

    //     Members member = membersRepository
    //             .findByUsername(username)
    //             .orElseThrow(()->new BaseException(ErrorCode.NO_USER_INFO));
    //     List<RefreshTokens> refreshTokens = refreshTokensRepository.findByUsername(username);
    //     for(RefreshTokens tokenToDelete : refreshTokens){
    //         refreshTokensRepository.delete(tokenToDelete);
    //     }
    //     membersRepository.delete(member);
    //     return member;
    // }

    @Transactional
    public List<String> getUniversitiesList(String unvToSearch){
        if(unvToSearch.matches(".*[^가-힣\\sA-Z()].*")){
            //BAD_INPUT
            throw new BaseException(ErrorCode.BAD_INPUT);
        }
        List<Universities> universities = universitiesRepository
                .searchByUniversity(unvToSearch);
        if(universities.isEmpty()){
            throw new BaseException(ErrorCode.NO_UNIVERSITY_INFO);
        }

        List<String> returnList = new ArrayList<>();
        for(Universities unv : universities){
            returnList.add(unv.getUniversity());
        }
        return returnList;
    }

    @Transactional
    public List<String> getMajorsList(String majorToSearch){
        if(majorToSearch.matches(".*[^가-힣\\sA-Z()].*")){
            //BAD_INPUT
            throw new BaseException(ErrorCode.BAD_INPUT);
        }
        List<Majors> majors = majorsRepository
                .searchByMajor(majorToSearch);

        if(majors.isEmpty()){
            throw new BaseException(ErrorCode.NO_MAJOR_INFO);
        }

        List<String> returnList = new ArrayList<>();
        for(Majors major : majors){
            returnList.add(major.getMajor());
        }
        return returnList;
    }

    // public UserinfoDTO getUserinfo(String token){
    //     if(Boolean.TRUE.equals(jwtUtil.isExpired(token))){
    //         //EXPIRED_TOKEN
    //         throw new BaseException(ErrorCode.EXPIRED_TOKEN);
    //     }

    //     String username = jwtUtil.getUsername(token);

    //     Members foundMember = membersRepository
    //             .findByUsername(username)
    //             .orElseThrow(()->new BaseException(ErrorCode.NO_USER_INFO));

    //     String returnNickname = "닉네임을 설정하세요.";
    //     if(foundMember.getNickname() != null){
    //         returnNickname = foundMember.getNickname();
    //     }

    //     String returnUnv = "대학교를 설정하세요.";
    //     if(foundMember.getUniversity() != null && foundMember.getUniversity() != -1L){
    //         Universities foundUnv = universitiesRepository
    //                 .findByUnvId(foundMember.getUniversity())
    //                 .orElseThrow(()->new BaseException(ErrorCode.NO_UNIVERSITY_INFO));
    //         returnUnv = foundUnv.getUniversity();
    //     }

    //     String returnMajors = "관심 전공을 설정하세요.";
    //     StringBuilder majorList = new StringBuilder();
    //     if(foundMember.getMajors()!= null){
    //         List<String> majorIdList = new ArrayList<>(Arrays.asList(foundMember.getMajors().split(",")));
    //         for(String id : majorIdList){
    //             majorList.append(majorsRepository
    //                     .findByMajorId(id)
    //                     .orElseThrow(()->new BaseException(ErrorCode.NO_MAJOR_INFO))
    //                     .getMajor());
    //             majorList.append(", ");
    //         }
    //         if (!majorList.isEmpty()) {
    //             majorList.setLength(majorList.length() - 2);
    //         }
    //         returnMajors = majorList.toString();
    //     }

    //     return UserinfoDTO
    //             .builder()
    //             .username(foundMember.getUsername())
    //             .nickname(returnNickname)
    //             .university(returnUnv)
    //             .majors(returnMajors)
    //             .build();
    // }
}

