package com.be.rebook.domain.members.service;

import com.be.rebook.domain.members.repository.RefreshTokensRepository;
import com.be.rebook.domain.members.entity.RefreshTokens;
import com.be.rebook.domain.members.jwt.JWTUtil;
import com.be.rebook.global.exception.BaseException;
import com.be.rebook.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;


@Service
public class ReissueService {
    private final JWTUtil jwtUtil;
    private final RefreshTokensRepository refreshRepository;

    public ReissueService(JWTUtil jwtUtil,
                          RefreshTokensRepository refreshTokensRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshTokensRepository;
    }
    public void deleteRefreshsOlderThanOneDay() {
        LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        String cutoff = cutoffDateTime.format(formatter);

        refreshRepository.deleteOldRefresh(cutoff);
    }

    public RefreshTokens reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // get refresh token
        String refresh = null;
        String refreshCategory = "refresh";
        String accessCategory = "access";
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(refreshCategory)) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            //NO_TOKEN_CONTENT
            throw new BaseException(ErrorCode.NO_TOKEN_CONTENT);
        }

        // expired check
        if(Boolean.TRUE.equals(jwtUtil.isExpired(refresh))) {
            throw new BaseException(ErrorCode.EXPIRED_TOKEN);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals(refreshCategory)) {
            //TOKEN_CATEGORY_INCORRECT
            throw new BaseException(ErrorCode.TOKEN_CATEGORY_INCORRECT);
        }

        //db에 리프레시 토큰이 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if(Boolean.FALSE.equals(isExist)){
            //NO_TOKEN_CONTENT
            throw new BaseException(ErrorCode.NO_TOKEN_CONTENT);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        String newAccess = jwtUtil.createJwt(accessCategory, username, role, 600000L);
        String newRefresh = jwtUtil.createJwt(refreshCategory, username, role, 86400000L);

        //리프레쉬 토큰 저장 db에 기존의 리프레시 토큰 삭제 후 새 리프레시 토큰 저장
        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(username,newRefresh,86400000L);

        // response
        response.setHeader(accessCategory, newAccess);
        response.addCookie(createCookie(refreshCategory, newRefresh));
        return RefreshTokens.builder().username(username).refresh(refresh).build();
    }
    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setHttpOnly(true);

        return cookie;
    }
    private void addRefreshEntity(String username, String refresh, Long expriedMs){
        Date date = new Date(System.currentTimeMillis()+ expriedMs);

        RefreshTokens refreshTokens = RefreshTokens.builder()
                .username(username)
                .refresh(refresh)
                .expiration(date.toString())
                .build();

        refreshRepository.save(refreshTokens);
    }
}
