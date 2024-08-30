package com.be.rebook.common.restclients;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import com.be.rebook.common.config.BaseResponse;
import com.be.rebook.common.dto.InternalMemberInfoDTO;

public interface MemberServiceRestClient {
  @GetExchange("/internal/members/info/{username}")
  ResponseEntity<BaseResponse<InternalMemberInfoDTO>> getMemberInfoByUsername(@PathVariable String username);
}
