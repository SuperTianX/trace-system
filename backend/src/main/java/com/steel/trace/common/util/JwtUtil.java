package com.steel.trace.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.steel.trace.common.constant.Constants;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            Constants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(String username, Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + Constants.JWT_EXPIRE_MS))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String generateRefreshToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + Constants.JWT_REFRESH_EXPIRE_MS))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public static Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }
}
