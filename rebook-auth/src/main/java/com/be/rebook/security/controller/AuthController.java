package com.be.rebook.security.controller;


import com.be.rebook.security.entity.Members;
import com.be.rebook.security.entity.RefreshTokens;
import com.be.rebook.security.service.JoinService;
import com.be.rebook.security.dto.JoinDTO;
import com.be.rebook.global.config.BaseResponse;
import com.be.rebook.security.service.ReissueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger joinLogger = LoggerFactory.getLogger(AuthController.class);
    private final JoinService joinService;

    private final ReissueService reissueService;

    public AuthController(JoinService joinService,
                          ReissueService reissueService){
        this.joinService = joinService;
        this.reissueService = reissueService;
    }

    //이거 Members Entity 여기다가 생성해줘야됨?
    @PostMapping("/join")
    public ResponseEntity<BaseResponse<Members>> joinProcess(@RequestBody JoinDTO joinDTO) {
        joinLogger.info(joinDTO.getUsername());
        return ResponseEntity.ok().body(new BaseResponse<>(joinService.joinProcess(joinDTO)));
    }

    @PostMapping("members/refreshtoken")
    public ResponseEntity<BaseResponse<RefreshTokens>> reissue(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok().body(new BaseResponse<>(reissueService.reissueToken(request, response)));
    }
}