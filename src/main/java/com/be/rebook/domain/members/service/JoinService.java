package com.be.rebook.domain.members.service;

import com.be.rebook.domain.members.entity.Members;
import com.be.rebook.domain.members.repository.MembersRepository;
import com.be.rebook.domain.members.dto.JoinDTO;
import com.be.rebook.domain.members.utility.InputVerifier;
import com.be.rebook.global.exception.BaseException;
import com.be.rebook.global.exception.ErrorCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final MembersRepository membersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(MembersRepository membersRepository, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.membersRepository = membersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Members joinProcess(JoinDTO joinDTO){
        String username = joinDTO.getUsername();

        if(Boolean.FALSE.equals(InputVerifier.checkUsernameCharacters(username))){
            //BAD_INPUT
            throw new BaseException(ErrorCode.BAD_INPUT);
        }

        String password = joinDTO.getPassword();
        if(Boolean.FALSE.equals(InputVerifier.checkPasswordCharacters(password))){
            //BAD_INPUT
            throw new BaseException(ErrorCode.BAD_INPUT);
        }

        Boolean isExist = membersRepository.existsByUsername(username);

        if(Boolean.TRUE.equals(isExist)){
            //EXISTING_USER_INFO
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
