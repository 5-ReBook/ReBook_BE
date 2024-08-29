package com.be.rebook.auth.oauth.handler;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.be.rebook.auth.jwt.JWTUtil;
import com.be.rebook.auth.jwt.type.TokenCategory;
import com.be.rebook.auth.utility.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Component
public class OAuthAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final String targetUrl;

    private final JWTUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public OAuthAuthenticationSuccessHandler(@Value("${info.web.oauth.targetUrl}") String targetUrl, JWTUtil jwtUtil,
                                             CookieUtil cookieUtil) {
        this.targetUrl = targetUrl;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
    }

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        super.clearAuthenticationAttributes(request);
        this.determineTargetUrl(request, response, authentication);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
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
        return targetUrl;
    }
}