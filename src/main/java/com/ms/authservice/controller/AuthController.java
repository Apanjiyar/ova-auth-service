package com.ms.authservice.controller;

import com.ms.authservice.dto.ApiResponse;
import com.ms.authservice.dto.LoginRequest;
import com.ms.authservice.dto.RegisterRequest;
import com.ms.authservice.dto.RegisterResponse;
import com.ms.authservice.service.AuthService;
import com.ms.authservice.util.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.JobKOctets;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
    RegisterResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseUtil.success(response, "User registered successfully"));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
    String token = authService.login(request);
    return ResponseEntity.ok(ApiResponseUtil.success(Map.of("token", token), "Login successful"));
  }
}