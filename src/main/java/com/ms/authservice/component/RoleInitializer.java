package com.ms.authservice.component;

import com.ms.authservice.entity.Role;
import com.ms.authservice.enums.RoleEnum;
import com.ms.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleInitializer {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void initializeRoles() {
        for (RoleEnum roleEnum : RoleEnum.values()) {
            String roleName = roleEnum.name();
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                role = roleRepository.save(role);
                log.info("Role: {} is created in db with ID: {}", roleName, role.getId());
            }
        }
    }
}
