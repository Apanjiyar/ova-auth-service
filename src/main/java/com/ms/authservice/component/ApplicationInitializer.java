package com.ms.authservice.component;

import com.ms.authservice.dto.RegisterRequest;
import com.ms.authservice.entity.Role;
import com.ms.authservice.entity.User;
import com.ms.authservice.enums.RoleEnum;
import com.ms.authservice.repository.RoleRepository;
import com.ms.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class ApplicationInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultUsersConfig defaultUsersConfig;

    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeDefaultUsers();
    }

    private void initializeRoles() {
        for (RoleEnum roleEnum : RoleEnum.values()) {
            String roleName = roleEnum.name();
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                role = roleRepository.save(role);
                log.info("Role: {} created in db with ID: {}", roleName, role.getId());
            }
        }
    }

    private void initializeDefaultUsers() {
        if (defaultUsersConfig.getUsers() == null || defaultUsersConfig.getUsers().isEmpty()) {
            return;
        }

        for (RegisterRequest userConfig : defaultUsersConfig.getUsers()) {
            if (userRepository.findByUsername(userConfig.getUsername()).isPresent()) {
                log.info("User {} already exists, skipping...", userConfig.getUsername());
                continue;
            }

            User user = new User();
            user.setUsername(userConfig.getUsername());
            user.setEmail(userConfig.getEmail());
            user.setPhone(userConfig.getPhone());
            user.setEmailVerified(false);
            user.setPhoneVerified(false);
            user.setPassword(passwordEncoder.encode(userConfig.getPassword()));

            Set<Role> roles = new HashSet<>();
            for (String roleName : userConfig.getRoles()) {
                Optional<Role> roleOptional = roleRepository.findByName(roleName);
                roleOptional.ifPresent(roles::add);
            }
            user.setRoles(roles);
            userRepository.save(user);
            log.info("Default user {} created with roles: {}", user.getUsername(), userConfig.getRoles());
        }
    }
}
