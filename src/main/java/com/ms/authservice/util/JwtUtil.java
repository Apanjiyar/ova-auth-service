package com.ms.authservice.util;

import com.ms.authservice.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

  private final JwtProperties jwtProperties;

  public String generateToken(UserDetails userDetails) {

    final String SECRET = jwtProperties.secret();
    final Long EXPIRATION_TIME_MS = jwtProperties.expirationTimeMs();

    return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("roles", userDetails.getAuthorities())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
            .compact();
  }

  public String extractUsername(String token) {
    final String SECRET = jwtProperties.secret();
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

  public Date getExpirationSeconds(String token) {
    final String SECRET = jwtProperties.secret();
    return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getExpiration();
  }
}