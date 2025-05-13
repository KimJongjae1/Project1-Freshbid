package com.example.loginapi.util;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil {
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 안전한 키 생성

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .signWith(secretKey)
                .compact();
    }
}
