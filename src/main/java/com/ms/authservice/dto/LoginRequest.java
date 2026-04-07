package com.ms.authservice.dto;

import com.ms.authservice.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

  @NotBlank
  private String identifier;

  @NotBlank(message = "Password is required")
  @ValidPassword
  private String password;
}