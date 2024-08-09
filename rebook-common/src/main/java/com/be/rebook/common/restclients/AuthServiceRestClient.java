package com.be.rebook.common.restclients;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;

import com.be.rebook.common.argumentresolver.auth.MemberLoginInfo;

public interface AuthServiceRestClient {

    @GetExchange("/auth/members/authenticate")
    MemberLoginInfo authenticate(@RequestHeader("Authorization") String token);
}
