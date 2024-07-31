package com.be.rebook.domain.members.jwt;

import com.be.rebook.domain.members.repository.RefreshTokensRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

public class CustomLogoutFilter extends GenericFilterBean {
    private final JWTUtil jwtUtil;

    private final RefreshTokensRepository refreshTokensRepository;

    public CustomLogoutFilter(JWTUtil jwtUtil,
                              RefreshTokensRepository refreshTokensRepository){
        this.jwtUtil = jwtUtil;
        this.refreshTokensRepository = refreshTokensRepository;
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException{

        //path and method verify
        String requestUri = request.getRequestURI();
        if (!requestUri.matches("^\\/logout$")) {

            filterChain.doFilter(request, response);
            return;
        }
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {

            filterChain.doFilter(request, response);
            return;
        }

        String refresh = null;
        String refreshCategory = "refresh";
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals(refreshCategory)) {

                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            //NO_TOKEN_CONTENT
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if(Boolean.TRUE.equals(jwtUtil.isExpired(refresh))){
            //EXPIRED_TOKEN
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals(refreshCategory)) {
            //TOKEN_CATEGORY_INCORRECT
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshTokensRepository.existsByRefresh(refresh);
        if (Boolean.FALSE.equals(isExist)) {
            //NO_TOKEN_CONTENT
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        refreshTokensRepository.deleteByRefresh(refresh);

        Cookie cookie = new Cookie(refreshCategory, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
