package com.be.rebook.auth.service;

import com.be.rebook.auth.dto.CustomUserDetails;
import com.be.rebook.auth.repository.MembersRepository;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final MembersRepository membersRepository;

    public CustomUserDetailService(MembersRepository membersRepository){
        this.membersRepository = membersRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new CustomUserDetails(membersRepository
                .findByUsername(username)
                .orElseThrow(()->new BaseException(ErrorCode.NO_USER_INFO)));
    }
}
