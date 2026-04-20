package com.ms.authservice.service;

import com.ms.authservice.dto.AssignRolesRequest;
import com.ms.authservice.dto.LoginRequest;
import com.ms.authservice.dto.RegisterRequest;
import com.ms.authservice.dto.RegisterResponse;
import com.ms.authservice.entity.RefreshToken;
import com.ms.authservice.entity.Role;
import com.ms.authservice.entity.User;
import com.ms.authservice.enums.RoleEnum;
import com.ms.authservice.exception.BadRequestException;
import com.ms.authservice.exception.UnauthorizedException;
import com.ms.authservice.exception.BusinessException;
import com.ms.authservice.exception.ResourceNotFoundException;
import com.ms.authservice.properties.JwtProperties;
import com.ms.authservice.properties.RedisPrefixProperties;
import com.ms.authservice.repository.RefreshTokenRepository;
import com.ms.authservice.repository.RoleRepository;
import com.ms.authservice.repository.UserRepository;
import com.ms.authservice.util.ApplicationUtil;
import com.ms.authservice.util.JwtUtil;
import com.ms.authservice.util.RedisUtilService;
import com.ms.authservice.util.RedisUtilServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final RedisUtilService redisUtilService;
  private final RedisPrefixProperties redisPrefixProperties;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProperties jwtProperties;

  public RegisterResponse register(RegisterRequest req) {
    // Either email or phone is required for registering a user
    if ((req.getEmail() == null || req.getEmail().isEmpty()) && (req.getPhone() == null || req.getPhone().isEmpty())) {
      throw new BadRequestException("Either email or phone is required");
    }

    if(req.getPhone() != null && !req.getPhone().isEmpty() && !ApplicationUtil.validatePhoneNumber(req.getPhone())){
      throw new BadRequestException("Invalid phone number format. Expected format: +[country-code]-[10-digit-number]");
    }

    Optional<User> userOptional = userRepository.findByUsername(req.getUsername());
    if (userOptional.isPresent()) {
      throw new BusinessException("Username already exists");
    }

    if (req.getEmail() != null && !req.getEmail().isEmpty()) {
      userOptional = userRepository.findByEmail(req.getEmail());
      if (userOptional.isPresent()) {
        throw new BusinessException("Email already exists");
      }
    }

    if (req.getPhone() != null && !req.getPhone().isEmpty()) {
      userOptional = userRepository.findByPhone(req.getPhone());
      if (userOptional.isPresent()) {
        throw new BusinessException("Phone number already exists");
      }
    }

    User user = new User();
    user.setUsername(req.getUsername());
    user.setEmail(req.getEmail());
    user.setPhone(req.getPhone());
    user.setEmailVerified(false);
    user.setPhoneVerified(false);
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    Role userRole = roleRepository.findByName(RoleEnum.USER.name()).orElseThrow(() -> new BusinessException("User role not found in system"));
    user.setRoles(Set.of(userRole));
    User savedUser = userRepository.save(user);
    return RegisterResponse.of(savedUser);
  }

  public Map<String, String> login(LoginRequest req) {
    User user = userRepository.findByUsername(req.getIdentifier())
        .or(() -> userRepository.findByEmail(req.getIdentifier()))
        .or(() -> userRepository.findByPhone(req.getIdentifier()))
        .orElseThrow(() -> new UnauthorizedException("Invalid username, email, or phone"));
    if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
      throw new UnauthorizedException("Invalid password");
    }
    String accessToken = jwtUtil.generateToken(user);
    String refreshToken = jwtUtil.getRefreshToken(user);
    Map<String, String> response = new HashMap<>();
    response.put("accessToken", accessToken);
    response.put("refreshToken", refreshToken);

    createRefreshToken(user, refreshToken);

    return response;
  }

  @Transactional
  public void createRefreshToken(User user, String refreshToken){
    RefreshToken token = new RefreshToken();
    token.setToken(refreshToken);
    token.setUserId(user.getId());
    token.setExpiresAt(Instant.now().plus(jwtProperties.refreshExpirationTimeMs(), ChronoUnit.MILLIS));
    token.setRevoked(false);
    refreshTokenRepository.save(token);
  }

  public Map<String, String> refresh(Map<String, String> request) {
    String oldRefreshToken = request.get("refreshToken");
    if(oldRefreshToken == null || oldRefreshToken.isEmpty()) {
      throw new BadRequestException("Invalid refresh token in payload");
    }

    // Validate JWT signature first
    if (!jwtUtil.validateTokenSignature(oldRefreshToken)) {
      throw new UnauthorizedException("Invalid refresh token");
    }
    String tokenUsername = jwtUtil.extractUsernameFromRefreshToken(oldRefreshToken);

    RefreshToken oldRtObject = refreshTokenRepository.findByToken(oldRefreshToken).orElse(null);

    if(oldRtObject == null) {
      throw new ResourceNotFoundException("Refresh token", "token", oldRefreshToken);
    }
    if(oldRtObject.isRevoked()){
      throw new UnauthorizedException("Refresh token already revoked");
    }
    if(oldRtObject.getExpiresAt().isBefore(Instant.now())) {
      throw new UnauthorizedException("Refresh token expired, please login");
    }

    // Validate username matches
    User user = userRepository.findById(oldRtObject.getUserId()).orElse(null);
    if(user == null){
      throw new ResourceNotFoundException("User", "id", oldRtObject.getUserId());
    }
    if(!user.getUsername().equals(tokenUsername)) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    revokeRefreshToken(oldRtObject);
    String accessToken = jwtUtil.generateToken(user);
    String refreshToken = jwtUtil.getRefreshToken(user);
    Map<String, String> response = new HashMap<>();
    response.put("accessToken", accessToken);
    response.put("refreshToken", refreshToken);

    createRefreshToken(user, refreshToken);

    return response;
  }

  @Transactional
  private void revokeRefreshToken(RefreshToken refreshToken) {
    refreshToken.setRevoked(true);
    refreshTokenRepository.save(refreshToken);
  }

  public String logout(String authHeader) {
    String token = authHeader.substring(7);
    Date expiration = jwtUtil.getExpirationSeconds(token);
    long remainingTime = expiration.getTime() - System.currentTimeMillis();
    if (remainingTime > 0) {
      final String AUTH_TOKEN_BLACKLIST = redisPrefixProperties.blacklistToken();
      String key = RedisUtilServiceImpl.buildKey(AUTH_TOKEN_BLACKLIST, token);
      redisUtilService.set(key, "blacklisted", remainingTime, TimeUnit.MILLISECONDS);
    }

    String username = jwtUtil.extractUsername(token);
    Optional<User> user = userRepository.findByUsername(username);
    user.ifPresent(value -> refreshTokenRepository.revokeAllByUserId(value.getId()));
    return "Logout successful";
  }

  public RegisterResponse assignRoles(AssignRolesRequest request) {
    User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername()));

    if (request.getRoles() == null || request.getRoles().isEmpty()) {
      throw new BadRequestException("At least one role is required");
    }

    Set<Role> roles = new HashSet<>();
    for (String roleName : request.getRoles()) {
      Role role = roleRepository.findByName(roleName).orElseThrow(() -> new BadRequestException("Invalid role: " + roleName));
      roles.add(role);
    }

    user.setRoles(roles);
    User savedUser = userRepository.save(user);
    return RegisterResponse.of(savedUser);
  }

  public RegisterResponse getUserInfo(String username) {
    User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    return RegisterResponse.of(user);
  }
}