package com.ms.authservice.service;

import com.ms.authservice.entity.User;
import com.ms.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return repo.findByUsername(username)
                .or(() -> repo.findByEmail(username))
                .or(() -> repo.findByPhone(username))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}