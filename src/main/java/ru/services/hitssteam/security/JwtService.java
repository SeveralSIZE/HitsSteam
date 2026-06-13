package ru.services.hitssteam.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String generate(String username, String role, Long userId) {
        return JWT.create()
                .withSubject(username)
                .withClaim("role", role)
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000L))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT parse(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
    }

    public String getUsername(String token) { return parse(token).getSubject(); }
    public String getRole(String token) { return parse(token).getClaim("role").asString(); }
    public Long getUserId(String token) { return parse(token).getClaim("userId").asLong(); }
}