package com.be.rebook.auth.jwt;

import com.be.rebook.auth.jwt.type.TokenCategory;
import com.be.rebook.auth.utility.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JWTUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public LoginFilter(AuthenticationManager authenticationManager,
                       JWTUtil jwtUtil,
                       CookieUtil cookieUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
        setFilterProcessesUrl("/auth/signin");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String username = null;
        String password = null;

        // JSON 파싱 리액트
        if (request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) {
            try {
                InputStream inputStream = request.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> requestMap = mapper.readValue(inputStream, Map.class);

                username = requestMap.getOrDefault("username", null);
                password = requestMap.getOrDefault("password", null);
                if (username == null || password == null) {
                    throw new AuthenticationServiceException("Need username and password");
                }
                password = password + username;

                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username,
                        password);
                return authenticationManager.authenticate(authRequest);
            } catch (IOException e) {
                throw new AuthenticationServiceException("Error parsing JSON request", e);
            }
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password,
                null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, Authentication authentication) {

        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String access = jwtUtil.createJwt(TokenCategory.ACCESS, username, role, TokenCategory.ACCESS.getExpiry());
        String refresh = jwtUtil.createJwt(TokenCategory.REFRESH, username, role, TokenCategory.REFRESH.getExpiry());

        jwtUtil.saveRefreshToken(username, refresh);

        response.setHeader(TokenCategory.ACCESS.getName(), access);
        cookieUtil.createCookie(TokenCategory.REFRESH.getName(), refresh,
                TokenCategory.REFRESH.getExpiry().intValue() / 1000, response);
        response.setStatus(HttpStatus.OK.value());
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) {
        // LOGIN_FAILED
        response.setStatus(401);
    }
}