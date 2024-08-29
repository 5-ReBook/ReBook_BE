package com.be.rebook.auth.controller;

import com.be.rebook.auth.dto.UpdatePasswordDTO;
import com.be.rebook.auth.dto.VerifyDTO;
import com.be.rebook.auth.entity.Members;
import com.be.rebook.auth.jwt.type.TokenCategory;
import com.be.rebook.auth.dto.CustomUserDetails;
import com.be.rebook.auth.dto.TokenDto;
import com.be.rebook.auth.dto.BasicUserInfoDTO;
import com.be.rebook.auth.service.ReissueService;
import com.be.rebook.auth.service.SignupService;
import com.be.rebook.auth.utility.CookieUtil;
import com.be.rebook.common.config.BaseResponse;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;
import com.be.rebook.common.argumentresolver.auth.MemberLoginInfo;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger signupLogger = LoggerFactory.getLogger(AuthController.class);
    private final SignupService signupService;

    private final ReissueService reissueService;
    private final CookieUtil cookieUtil;

    public AuthController(SignupService signupService,
                          ReissueService reissueService,
                          CookieUtil cookieUtil) {
        this.signupService = signupService;
        this.reissueService = reissueService;
        this.cookieUtil = cookieUtil;
    }

    @PostMapping("/members/signup")
    public ResponseEntity<BaseResponse<Members>> signupProcess(HttpServletRequest request,
                                                               @Valid @RequestBody BasicUserInfoDTO basicUserInfoDTO) {
        signupLogger.info(basicUserInfoDTO.getUsername());
        return ResponseEntity.ok().body(new BaseResponse<>(signupService.signupProcess(request, basicUserInfoDTO)));
    }

    @PostMapping("/members/refreshtoken/reissue")
    public ResponseEntity<BaseResponse<TokenDto>> reissueRefreshToken(HttpServletRequest request,
                                                                      HttpServletResponse response) {

        Cookie refreshTokenCookie = cookieUtil.findCookieFromRequest(TokenCategory.REFRESH.getName(), request);
        if (refreshTokenCookie == null) {
            throw new BaseException(ErrorCode.NO_TOKEN_CONTENT); // TODO : 적절한 Exception 처리
        }

        TokenDto newToken = reissueService.reissueToken(refreshTokenCookie.getValue());

        response.setHeader(TokenCategory.ACCESS.getName(), newToken.getAccessToken());
        cookieUtil
                .createCookie(TokenCategory.REFRESH.getName(),
                        newToken.getRefreshToken(),
                        TokenCategory.REFRESH.getExpiry().intValue() / 1000, response);
        return ResponseEntity.ok().body(new BaseResponse<TokenDto>(newToken));
    }

    @PostMapping("/members/signup/mail")
    public ResponseEntity<BaseResponse<Members>> emailVerify(@Email @RequestParam String username) {
        signupLogger.info(username);
        return ResponseEntity.ok().body(new BaseResponse<>(signupService.sendVerification(username)));
    }

    @PostMapping("/members/signup/verify")
    public ResponseEntity<BaseResponse<Members>> codeMatch(HttpServletResponse response,
                                                           @Valid @RequestBody VerifyDTO verifyDTO) {
        return ResponseEntity.ok().body(new BaseResponse<>(signupService.verifyCode(verifyDTO, response)));
    }

    @PostMapping("/members/authenticate")
    public ResponseEntity<BaseResponse<MemberLoginInfo>> authenticate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            // 필요한 데이터를 DTO로 변환
            MemberLoginInfo memberLoginInfo = new MemberLoginInfo(userDetails.getUsername(), userDetails.getRoleName());
            return ResponseEntity.ok().body(new BaseResponse<>(memberLoginInfo));
        } else {
            throw new BaseException(ErrorCode.UNAUTHORIZED); // TODO: 적절한 Exception 처리
        }
    }

    @PatchMapping("/members/password/reset")
    public ResponseEntity<BaseResponse<Members>> resetUserPassword(HttpServletRequest request,
                                                                   @Valid @RequestBody BasicUserInfoDTO resetPasswordDTO) {
        return ResponseEntity.ok()
                .body(new BaseResponse<>(reissueService.reissueUserPassword(request, resetPasswordDTO)));
    }

    // 로그인 이후 마이페이지에서 바로 비밀번호 변경이라 이메일 인증 필요없음
    @PatchMapping("/members/password")
    public ResponseEntity<BaseResponse<Members>> updateUserPassword(HttpServletRequest request,
                                                                    @Valid @RequestBody UpdatePasswordDTO passwordDTO) {
        String passwordToUpdate = passwordDTO.getPassword();
        return ResponseEntity.ok()
                .body(new BaseResponse<>(reissueService.updateUserPassword(request, passwordToUpdate)));
    }
}