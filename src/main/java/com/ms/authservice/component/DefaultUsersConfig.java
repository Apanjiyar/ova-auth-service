package com.ms.authservice.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.authservice.dto.RegisterRequest;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Data
@Component
public class DefaultUsersConfig {

    private List<RegisterRequest> users;

    @PostConstruct
    public void loadUsers() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/default-users.json");
        ObjectMapper mapper = new ObjectMapper();
        users = mapper.readValue(resource.getInputStream(), new TypeReference<List<RegisterRequest>>() {});
        log.info("Loaded {} default users from JSON", users.size());
    }
}