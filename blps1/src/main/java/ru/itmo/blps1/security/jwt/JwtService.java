package ru.itmo.blps1.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-minutes}")
    private long expirationMinutes;

    public String generateToken(String username, Set<String> roles) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationMinutes * 60);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        Object roles = extractAllClaims(token).get("roles");

        if (roles instanceof Set<?> roleSet) {
            return (Set<String>) roleSet;
        }

        if (roles instanceof Iterable<?> iterable) {
            Set<String> result = new java.util.HashSet<>();
            for (Object role : iterable) {
                result.add(String.valueOf(role));
            }
            return result;
        }

        return Set.of();
    }

    public boolean isTokenValid(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration().after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}