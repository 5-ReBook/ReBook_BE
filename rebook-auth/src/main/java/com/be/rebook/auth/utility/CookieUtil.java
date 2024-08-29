package com.be.rebook.auth.utility;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieUtil {
    @Value("${cookie.domain}")
    private String cookieDomain;

    public Cookie findCookieFromRequest(String key, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(key)) {
                return cookie;
            }
        }

        return null;
    }

    public void createCookie(String key, String value, int maxAge, HttpServletResponse response) {
        ResponseCookie cookie;
        if(cookieDomain.equals("localhost")){
            cookie = ResponseCookie.from(key, value)
                    .domain(cookieDomain)
                    .path("/")
                    .httpOnly(true)
                    .maxAge(maxAge)
                    .build();
        }
        else {
            cookie = ResponseCookie.from(key, value)
                    .domain(cookieDomain)
                    .path("/")
                    .httpOnly(true)
                    .maxAge(maxAge)
                    .secure(true)
                    .sameSite("None")
                    .build();
        }

        // 응답에 Set-Cookie 헤더로 추가
        response.addHeader("Set-Cookie", cookie.toString());
    }
}