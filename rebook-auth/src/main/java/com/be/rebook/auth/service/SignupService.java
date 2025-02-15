package com.be.rebook.auth.service;

import com.be.rebook.auth.dto.SignupDTO;
import com.be.rebook.auth.dto.VerifyDTO;
import com.be.rebook.auth.entity.Members;
import com.be.rebook.auth.jwt.JWTUtil;
import com.be.rebook.auth.jwt.type.TokenCategory;
import com.be.rebook.auth.repository.MembersRepository;
import com.be.rebook.auth.utility.CookieUtil;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import org.slf4j.Logger;

@Service
public class SignupService {

    private final MembersRepository membersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final RedisManagerImpl redisManager;

    private final EmailService emailService;

    private final JWTUtil jwtUtil;
    private final CookieUtil cookieUtil;

    private static final Logger signupLogger = LoggerFactory.getLogger(SignupService.class);

    public SignupService(MembersRepository membersRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            RedisManagerImpl redisManager,
            EmailService emailService,
            JWTUtil jwtUtil,
            CookieUtil cookieUtil) {
        this.membersRepository = membersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.redisManager = redisManager;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
    }

    @Transactional
    public Members signupProcess(HttpServletRequest request, SignupDTO signupDTO) {
        String mailToken = signupDTO.getMailauth();
        signupLogger.info("mailToken received : ");
        signupLogger.info(mailToken);
        mailToken = mailToken.substring(7);

        if (mailToken == null) {
            signupLogger.error("Authorization header is missing");
            // NO_TOKEN_CONTENT
            throw new BaseException(ErrorCode.NO_TOKEN_CONTENT);
        }

        // expired check
        if (Boolean.TRUE.equals(jwtUtil.isExpired(mailToken))) {
            signupLogger.error("Authorization header token is expired");
            throw new BaseException(ErrorCode.EXPIRED_TOKEN);
        }

        String username = signupDTO.getUsername();
        String password = signupDTO.getPassword();
        Boolean isExist = membersRepository.existsByUsername(username);
        if (Boolean.TRUE.equals(isExist)) {
            // EXISTING_USER_INFO
            throw new BaseException(ErrorCode.EXISTING_USER_INFO);
        }

        Members data = Members.builder()
                .username(username)
                .password(bCryptPasswordEncoder.encode(password + username))
                .role("ROLE_USER")
                .build();

        membersRepository.save(data);
        return Members.builder()
                .username(username)
                .build();
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000; // 100000 ~ 999999 사이의 숫자
        return String.valueOf(code);
    }

    public Members sendVerification(String username) {
        // 6자리 랜덤 인증번호 생성
        String verificationCode = generateVerificationCode();

        // Redis에 인증번호 저장 (키: username, 값: verificationCode, 유효시간: 3분)
        redisManager.setValuesWithDuration(username, verificationCode);

        // 이메일 전송
        emailService.sendVerificationEmail(username, verificationCode);

        return Members.builder()
                .username(username)
                .build();
    }

    public Members verifyCode(VerifyDTO verifyDTO, HttpServletResponse response) {
        String username = verifyDTO.getUsername();
        String code = verifyDTO.getCode();
        String storedVerificationCode = null;

        storedVerificationCode = redisManager.getValue(username);

        if (!storedVerificationCode.equals(code)) {
            // BAD_REQUEST
            throw new BaseException(ErrorCode.MAIL_AUTH_CODE_INCORRECT);
        } else {
            redisManager.deleteValue(username);
            String mailToken = jwtUtil
                    .createJwt(TokenCategory.MAILAUTH,
                            username,
                            "ROLE_USER",
                            TokenCategory.MAILAUTH.getExpiry());
            response.setHeader("Authorization", mailToken);
        }

        return Members.builder()
                .username(username)
                .build();
    }
}
