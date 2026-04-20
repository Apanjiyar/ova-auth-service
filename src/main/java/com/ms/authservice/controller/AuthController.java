package com.ms.authservice.controller;

import com.ms.authservice.dto.ApiResponse;
import com.ms.authservice.dto.AssignRolesRequest;
import com.ms.authservice.dto.LoginRequest;
import com.ms.authservice.dto.RegisterRequest;
import com.ms.authservice.dto.RegisterResponse;
import com.ms.authservice.service.AuthService;
import com.ms.authservice.util.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginRequest request) {
    Map<String, String> response = authService.login(request);
    return ResponseEntity.ok(ApiResponseUtil.success(response, "Login successful"));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@RequestBody Map<String, String> request) {
    Map<String, String> response = authService.refresh(request);
    return ResponseEntity.ok(ApiResponseUtil.success(response, "Token refreshed successfully"));

  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
    String response = authService.logout(authHeader);
    return ResponseEntity.ok(ApiResponseUtil.success(null, response));
  }

  @PostMapping("/assign-roles")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<RegisterResponse>> assignRoles(@Valid @RequestBody AssignRolesRequest request){
    RegisterResponse response = authService.assignRoles(request);
    return ResponseEntity.ok(ApiResponseUtil.success(response, "Role assigned successfully"));
  }

  @GetMapping("/get-user-info")
  public ResponseEntity<ApiResponse<RegisterResponse>> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
    RegisterResponse response = authService.getUserInfo(userDetails.getUsername());
    return ResponseEntity.ok(ApiResponseUtil.success(response, "User info retrieved successfully"));
  }
}