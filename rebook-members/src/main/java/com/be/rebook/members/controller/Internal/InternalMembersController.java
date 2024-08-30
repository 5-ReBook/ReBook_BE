package com.be.rebook.members.controller.Internal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.be.rebook.common.config.BaseResponse;
import com.be.rebook.common.dto.InternalMemberInfoDTO;
import com.be.rebook.members.dto.OtherUserinfoDTO;
import com.be.rebook.members.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/members")
public class InternalMembersController {

  private final MemberService memberService;

  @GetMapping("/info/{username}")
  public ResponseEntity<BaseResponse<InternalMemberInfoDTO>> getUserinfo(@PathVariable String username) {

    OtherUserinfoDTO userInfo = memberService.getOtherUserinfo(username);

    InternalMemberInfoDTO response = new InternalMemberInfoDTO(userInfo.getMemberId(),
        userInfo.getUsername(), userInfo.getNickname(), userInfo.getUniversity(), userInfo.getMajors(),
        userInfo.getStoredFileName());

    return ResponseEntity.ok().body(new BaseResponse<>(response));
  }
}
