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

  public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("roles", userDetails.getAuthorities())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
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