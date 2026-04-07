package com.ms.authservice.dto;

import com.ms.authservice.entity.Role;
import com.ms.authservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String username;
    private String email;
    private boolean emailVerified;
    private String phone;
    private boolean phoneVerified;
    private Set<String> roles;

    public static RegisterResponse of(User user) {
        return RegisterResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .emailVerified(user.getEmailVerified())
                .phone(user.getPhone())
                .phoneVerified(user.getPhoneVerified())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
