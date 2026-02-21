package com.nilesh.cym.token;

import com.nilesh.cym.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtService(
            @Value("${security.jwt.secret:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef}") String secret,
            @Value("${security.jwt.access-token-ttl:PT15M}") Duration accessTokenTtl,
            @Value("${security.jwt.refresh-token-ttl:P30D}") Duration refreshTokenTtl
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public TokenPair issueTokenPair(UserEntity user, String deviceSession) {
        Instant now = Instant.now();
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        Instant accessExpiry = now.plus(accessTokenTtl);
        Instant refreshExpiry = now.plus(refreshTokenTtl);

        String accessToken = issueToken(user, accessJti, "access", now, accessExpiry);
        String refreshToken = issueToken(user, refreshJti, "refresh", now, refreshExpiry);

        return new TokenPair(accessToken, refreshToken, accessJti, refreshJti, accessExpiry, refreshExpiry, deviceSession);
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
    }

    public boolean isTokenValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isRefreshToken(Jws<Claims> claimsJws) {
        return "refresh".equals(claimsJws.getPayload().get("typ", String.class));
    }

    private String issueToken(UserEntity user, String jti, String tokenType, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .claim("mob", user.getMob())
                .claim("typ", tokenType)
                .id(jti)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public record TokenPair(
            String accessToken,
            String refreshToken,
            String accessJti,
            String refreshJti,
            Instant accessExpiresAt,
            Instant refreshExpiresAt,
            String deviceSession
    ) {
    }
}
