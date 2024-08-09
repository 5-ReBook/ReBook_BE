package com.be.rebook.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.be.rebook.auth.jwt.type.TokenCategory;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.exception.ErrorCode;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {
    private final SecretKey secretKey;
    private static final Logger jwtUtilLogger = LoggerFactory.getLogger(JWTUtil.class);

    private JWTUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getUsername(String token) {
        return this.parseJwt(token).get("username", String.class);
    }

    public String getRole(String token) {
        return this.parseJwt(token).get("Role", String.class);
    }

    public Boolean isExpired(String token) {
        return this.parseJwt(token).getExpiration().before(new Date());
    }

    // 토큰 종류 구별용
    public TokenCategory getCategory(String token) {
        String category = this.parseJwt(token).get("category", String.class);
        return TokenCategory.getEnumFromValue(category);

    }

    public String createJwt(TokenCategory category, String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category.getName())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseJwt(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build().parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            jwtUtilLogger.error("JWT parsing failed: {}", e.getMessage());
            throw new BaseException(ErrorCode.EXPIRED_TOKEN); // TODO: 적절한 Exception으로 변경
        } catch (JwtException e) {
            jwtUtilLogger.error("JWT parsing failed: {}", e.getMessage());
            throw new BaseException(ErrorCode.NO_TOKEN_CONTENT); // TODO: 적절한 Exception으로 변겨
        }
    }
}
