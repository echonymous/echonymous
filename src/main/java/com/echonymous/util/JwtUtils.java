package com.echonymous.util;

import com.echonymous.dto.ApiResponseDTO;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())  // User ID as String
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build();
            Claims claims = parser.parseClaimsJws(token).getBody();

            Date expirationDate = claims.getExpiration();
            if (expirationDate.before(new Date())) {
                //return false; // Token is expired
                throw new ExpiredJwtException(null, claims, "Token is expired");
            }
            log.debug("Token in valid.");
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        JwtParser parser = Jwts.parser()
                .setSigningKey(secretKey)
                .build();
        Claims claims = parser.parseClaimsJws(token).getBody();
        return Long.parseLong(claims.getSubject());
    }

    public String extractJwtFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // Extract token after "Bearer "
        }
        return null;
    }
}
