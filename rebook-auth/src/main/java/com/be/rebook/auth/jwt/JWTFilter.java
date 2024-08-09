package com.be.rebook.auth.jwt;

import com.be.rebook.auth.dto.CustomUserDetails;
import com.be.rebook.auth.entity.Members;
import com.be.rebook.auth.jwt.type.TokenCategory;
import com.be.rebook.common.exception.ErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private static final String ACCESSTOKEN_HEADER = "Authorization";

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestUri = request.getRequestURI();

            // 로그인이나 관련 요청이 왔을땐 다음 필터로 넘겨서
            // 재로그인 무한 루프를 방지함
            // if (requestUri.matches("^\\/signin(?:\\/.*)?$")) {

            // filterChain.doFilter(request, response);
            // return;
            // }

            String accessToken = request.getHeader(ACCESSTOKEN_HEADER);

            if (accessToken == null) {
                // 권한이 필요 없는 경우도 있으니까
                filterChain.doFilter(request, response);

                return;
            }
            accessToken = accessToken.substring(7);

            // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
            if (Boolean.TRUE.equals(jwtUtil.isExpired(accessToken))) {
                // response status code = 401
                // 프론트에서 알아야됨
                // EXPIRED_TOKEN
                response.setStatus(ErrorCode.EXPIRED_TOKEN.getStatus().value());
                return;
            }

            // 토큰이 access인지 확인 (발급시 페이로드에 명시)
            TokenCategory category = jwtUtil.getCategory(accessToken);

            if (category != TokenCategory.ACCESS) {
                // TOKEN_CATEGORY_INCORRECT
                response.setStatus(ErrorCode.TOKEN_CATEGORY_INCORRECT.getStatus().value());
                return;
            }

            String username = jwtUtil.getUsername(accessToken);
            String role = jwtUtil.getRole(accessToken);

            Members members = Members.builder()
                    .username(username)
                    .role(role)
                    .build();

            CustomUserDetails customUserDetails = new CustomUserDetails(members);

            Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null,
                    customUserDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        } catch (Exception e) { // TODO : 적절한 Exception으로 변경
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
