package com.ms.authservice.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

  @Value("${app.jwt.secret}")
  private String SECRET;

  @Value("${app.jwt.expiration-time-ms}")
  private Long EXPIRATION_TIME_MS;

  public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("roles", userDetails.getAuthorities())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
            .compact();
  }

  public String extractUsername(String token) {
    return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
  }

  public boolean validateToken(String token, UserDetails userDetails) {
    return extractUsername(token).equals(userDetails.getUsername());
  }
}