package com.be.rebook.domain.members.service;

import com.be.rebook.domain.members.entity.Members;
import com.be.rebook.domain.members.repository.MembersRepository;
import com.be.rebook.domain.members.dto.JoinDTO;
import com.be.rebook.domain.members.utility.InputVerifier;
import com.be.rebook.global.exception.BaseException;
import com.be.rebook.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final MembersRepository membersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger joinServiceLogger = LoggerFactory.getLogger(JoinService.class);

    public JoinService(MembersRepository membersRepository, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.membersRepository = membersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Members joinProcess(JoinDTO joinDTO){
        joinServiceLogger.info("회원 가입 로직 시작");
        String username = joinDTO.getUsername();

        if(Boolean.FALSE.equals(InputVerifier.checkUsernameCharacters(username))){
            //BAD_INPUT
            joinServiceLogger.error("회원 가입 로직 오류 : 아이디에 특수문자 포함됨, 코드: {}", ErrorCode.BAD_INPUT);
            throw new BaseException(ErrorCode.BAD_INPUT);
        }

        String password = joinDTO.getPassword();
        if(Boolean.FALSE.equals(InputVerifier.checkPasswordCharacters(password))){
            //BAD_INPUT
            joinServiceLogger.error("회원 가입 로직 오류 : 비밀번호 너무 짧음, 코드: {}", ErrorCode.BAD_INPUT);
            throw new BaseException(ErrorCode.BAD_INPUT);
        }
        //todo : test -> 여기서 공격은 아닌데 비밀번호가 왜곡될 가능성이 있는지?
        //password = InputVerifier.sanitizeInput(password);
        //joinServiceLogger.info("회원 가입 로직 : 바뀐 비밀번호 {}", password);

        Boolean isExist = membersRepository.existsByUsername(username);

        if(Boolean.TRUE.equals(isExist)){
            //EXISTING_USER_INFO
            joinServiceLogger.error("회원 가입 로직 오류 : 이미 존재하는 아이디, 코드: {}", ErrorCode.EXISTING_USER_INFO);
            throw new BaseException(ErrorCode.EXISTING_USER_INFO);
        }

        Members data = Members.builder()
                .username(username)
                .password(bCryptPasswordEncoder.encode(password+username))
                .role("ROLE_USER")
                .build();

        membersRepository.save(data);
        return data;
    }
}
