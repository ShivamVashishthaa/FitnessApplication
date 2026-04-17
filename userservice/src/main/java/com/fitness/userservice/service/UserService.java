package com.fitness.userservice.service;

import com.fitness.userservice.models.User;
import com.fitness.userservice.models.dto.RegisterRequest;
import com.fitness.userservice.models.dto.UserResponse;
import com.fitness.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserResponse register(RegisterRequest registerRequest) {

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            User existingUser = userRepository.findByEmail(registerRequest.getEmail());
            UserResponse.builder()
                    .id(existingUser.getId())
                    .email(existingUser.getEmail())
                    .password(existingUser.getPassword())
                    .firstName(existingUser.getFirstName())
                    .lastName(existingUser.getLastName())
                    .createdAt(existingUser.getCreatedAt())
                    .updatedAt(existingUser.getUpdatedAt())
                    .build();
        }
        User user = User.builder().
                email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .build();
        User savedUser = userRepository.save(user);
        return mapUserToUserResponse(savedUser);
    }

    private UserResponse mapUserToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserResponse getUserProfile(String userId) {
        return userRepository.findById(userId).map(this::mapUserToUserResponse)
                .orElseThrow(() -> new RuntimeException("User Not found"));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapUserToUserResponse).toList();
    }

    public Boolean existByUserId(String userId) {
        log.info("Calling User Service For {}", userId);
        return userRepository.existsByKeycloakId(userId);
    }


}
