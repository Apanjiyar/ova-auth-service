package com.ms.authservice.dto;

import com.ms.authservice.validation.EmailOrPhoneRequired;
import com.ms.authservice.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@EmailOrPhoneRequired
public class RegisterRequest {

  @NotBlank(message = "Username is required")
  private String username;

  @Email(message = "Email should be valid")
  private String email;

  private String phone;

  @NotBlank(message = "Password is required")
  @ValidPassword
  private String password;
}