package com.ms.authservice.service;

import com.ms.authservice.dto.AssignRolesRequest;
import com.ms.authservice.dto.LoginRequest;
import com.ms.authservice.dto.RegisterRequest;
import com.ms.authservice.dto.RegisterResponse;
import com.ms.authservice.entity.Role;
import com.ms.authservice.entity.User;
import com.ms.authservice.enums.RoleEnum;
import com.ms.authservice.exception.BadRequestException;
import com.ms.authservice.exception.UnauthorizedException;
import com.ms.authservice.exception.BusinessException;
import com.ms.authservice.exception.ResourceNotFoundException;
import com.ms.authservice.repository.RoleRepository;
import com.ms.authservice.repository.UserRepository;
import com.ms.authservice.util.ApplicationUtil;
import com.ms.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

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

  public String login(LoginRequest req) {
    User user = userRepository.findByUsername(req.getIdentifier())
        .or(() -> userRepository.findByEmail(req.getIdentifier()))
        .or(() -> userRepository.findByPhone(req.getIdentifier()))
        .orElseThrow(() -> new UnauthorizedException("Invalid username, email, or phone"));
    if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
      throw new UnauthorizedException("Invalid password");
    }
    return jwtUtil.generateToken(user);
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