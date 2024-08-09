package com.be.rebook.auth.jwt;

import com.be.rebook.auth.entity.RefreshTokens;
import com.be.rebook.auth.jwt.type.TokenCategory;
import com.be.rebook.auth.repository.RefreshTokensRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
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
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JWTUtil jwtUtil;

    private final RefreshTokensRepository refreshTokensRepository;
    public LoginFilter(AuthenticationManager authenticationManager,
                       JWTUtil jwtUtil,
                       RefreshTokensRepository refreshTokensRepository){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokensRepository = refreshTokensRepository;
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


                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
                return authenticationManager.authenticate(authRequest);
            } catch (IOException e) {
                throw new AuthenticationServiceException("Error parsing JSON request", e);
            }
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username,password,null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, Authentication authentication){

        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends  GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String access = jwtUtil.createJwt(TokenCategory.ACCESS, username, role, 600000L);
        String refresh = jwtUtil.createJwt(TokenCategory.REFRESH, username, role, 86400000L);

        addRefreshEntity(username, refresh, 86400000L);

        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed){
        //LOGIN_FAILED
        response.setStatus(401);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //자바스크립트로 해당 쿠키 접근 못하게
        cookie.setHttpOnly(true);

        return cookie;
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshTokens refreshTokens = RefreshTokens.builder()
                .username(username)
                .refresh(refresh)
                .expiration(date.toString())
                .build();

        refreshTokensRepository.save(refreshTokens);
    }
}
