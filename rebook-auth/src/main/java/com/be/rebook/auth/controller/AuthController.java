package com.be.rebook.auth.controller;

import com.be.rebook.auth.entity.Members;
import com.be.rebook.auth.entity.RefreshTokens;
import com.be.rebook.auth.dto.CustomUserDetails;
import com.be.rebook.auth.dto.SignupDTO;
import com.be.rebook.auth.service.ReissueService;
import com.be.rebook.auth.service.SignupService;
import com.be.rebook.common.config.BaseResponse;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;
import com.be.rebook.common.argumentresolver.auth.MemberLoginInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger signupLogger = LoggerFactory.getLogger(AuthController.class);
    private final SignupService signupService;

    private final ReissueService reissueService;

    public AuthController(SignupService signupService,
            ReissueService reissueService) {
        this.signupService = signupService;
        this.reissueService = reissueService;
    }

    @PostMapping("/members/signup")
    public ResponseEntity<BaseResponse<Members>> signupProcess(@Valid @RequestBody SignupDTO signupDTO) {
        signupLogger.info(signupDTO.getUsername());
        return ResponseEntity.ok().body(new BaseResponse<>(signupService.signupProcess(signupDTO)));
    }

    @PostMapping("/members/refreshtoken/reissue")
    public ResponseEntity<BaseResponse<RefreshTokens>> reissue(HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok().body(new BaseResponse<>(reissueService.reissueToken(request, response)));
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
}