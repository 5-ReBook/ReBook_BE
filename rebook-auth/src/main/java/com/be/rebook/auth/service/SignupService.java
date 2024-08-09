package com.be.rebook.auth.service;

import com.be.rebook.auth.entity.Members;
import com.be.rebook.auth.dto.SignupDTO;
import com.be.rebook.auth.repository.MembersRepository;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupService {

    private final MembersRepository membersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SignupService(MembersRepository membersRepository, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.membersRepository = membersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public Members signupProcess(SignupDTO singupDTO){
        String username = singupDTO.getUsername();
        String password = singupDTO.getPassword();
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
