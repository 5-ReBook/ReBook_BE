package com.be.rebook.auth.service;

import com.be.rebook.auth.dto.BasicUserInfoDTO;
import com.be.rebook.auth.dto.TokenDto;
import com.be.rebook.auth.entity.Members;
import com.be.rebook.auth.repository.MembersRepository;
import com.be.rebook.auth.utility.CookieUtil;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;
import com.be.rebook.auth.jwt.JWTUtil;
import com.be.rebook.auth.jwt.type.TokenCategory;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

@Service
public class ReissueService {
    private final JWTUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MembersRepository membersRepository;

    public ReissueService(JWTUtil jwtUtil,
                          MembersRepository membersRepository,
                          BCryptPasswordEncoder bCryptPasswordEncoder,
                          CookieUtil cookieUtil) {
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
        this.membersRepository = membersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public Members reissueUserPassword(HttpServletRequest request, BasicUserInfoDTO resetPasswordDTO) {
        String mailToken = null;

        Cookie mailCookie = cookieUtil.findCookieFromRequest(TokenCategory.MAILAUTH.getName(), request);
        if (mailCookie == null) {
            throw new BaseException(ErrorCode.NO_TOKEN_CONTENT);
        }
        mailToken = mailCookie.getValue();

        if (mailToken == null) {
            // NO_TOKEN_CONTENT
            throw new BaseException(ErrorCode.NO_TOKEN_CONTENT);
        }

        // expired check
        if (Boolean.TRUE.equals(jwtUtil.isExpired(mailToken))) {
            throw new BaseException(ErrorCode.EXPIRED_TOKEN);
        }

        String username = resetPasswordDTO.getUsername();
        String newPassword = resetPasswordDTO.getPassword();

        Members member = membersRepository.findByUsername(username)
                .orElseThrow(() -> new BaseException(ErrorCode.NO_USER_INFO));

        member = member.toBuilder()
                .password(bCryptPasswordEncoder.encode(newPassword + username))
                .build();

        membersRepository.save(member);
        return Members.builder()
                .username(username)
                .build();
    }

    @Transactional
    public Members updateUserPassword(HttpServletRequest request, String passwordToUpdate) {
        String accessToken = request.getHeader("Authorization");

        if (accessToken == null) {
            // NO_TOKEN_CONTENT
            throw new BaseException(ErrorCode.NO_TOKEN_CONTENT);
        }

        accessToken = accessToken.substring(7);

        if (Boolean.TRUE.equals(jwtUtil.isExpired(accessToken))) {
            throw new BaseException(ErrorCode.EXPIRED_TOKEN);
        }

        String username = jwtUtil.getUsername(accessToken);

        Members member = membersRepository.findByUsername(username)
                .orElseThrow(() -> new BaseException(ErrorCode.NO_USER_INFO));

        Members updatedMember = member
                .toBuilder()
                .password(bCryptPasswordEncoder.encode(passwordToUpdate + username))
                .build();

        membersRepository.save(updatedMember);
        return Members.builder()
                .username(username)
                .build();
    }

    public TokenDto reissueToken(String oldRefreshToken) {
        if (oldRefreshToken == null) {
            // NO_TOKEN_CONTENT
            throw new BaseException(ErrorCode.NO_TOKEN_CONTENT);
        }

        // expired check
        if (Boolean.TRUE.equals(jwtUtil.isExpired(oldRefreshToken))) {
            throw new BaseException(ErrorCode.EXPIRED_TOKEN);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        TokenCategory category = jwtUtil.getCategory(oldRefreshToken);

        if (category != TokenCategory.REFRESH) {
            // TOKEN_CATEGORY_INCORRECT
            throw new BaseException(ErrorCode.TOKEN_CATEGORY_INCORRECT);
        }

        String username = jwtUtil.getUsername(oldRefreshToken);
        String role = jwtUtil.getRole(oldRefreshToken);

        // Redis에 리프레시 토큰이 저장되어 있는지 확인
        String foundRefreshToken = jwtUtil.getRefreshTokenByUsername(username);
        if (foundRefreshToken == null || !foundRefreshToken.equals(oldRefreshToken)) {
            // Token이 Redis에 저장되어 있는 것과 다름 (유효하지 않음)
            throw new BaseException(ErrorCode.TOKEN_NOT_VALID);
        }

        String newAccess = jwtUtil.createJwt(TokenCategory.ACCESS, username, role, TokenCategory.ACCESS.getExpiry());
        String newRefresh = jwtUtil.createJwt(TokenCategory.REFRESH, username, role, TokenCategory.REFRESH.getExpiry());

        // 리프레쉬 토큰 저장 db에 기존의 리프레시 토큰 삭제 후 새 리프레시 토큰 저장
        jwtUtil.saveRefreshToken(username, newRefresh);
        return TokenDto.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .build();
    }
}