package com.Vehicle.service.management.System.service;

import com.Vehicle.service.management.System.dto.LoginRequest;
import com.Vehicle.service.management.System.dto.RegisterRequest;
import com.Vehicle.service.management.System.entity.User;
import com.Vehicle.service.management.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(RegisterRequest registerRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Validate password match
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Validate that user is not trying to register as Admin or Mechanic
        if (registerRequest.getRole() == User.UserRole.ADMIN ||
                registerRequest.getRole() == User.UserRole.MECHANIC) {
            throw new RuntimeException("Admin and Mechanic accounts cannot be created through registration");
        }

        // Create user entity
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword()); // Store plain text password
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());
        user.setAddress(registerRequest.getAddress());
        user.setRole(registerRequest.getRole());

        return userRepository.save(user);
    }

    public User loginUser(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOptional.get();

        // Check password (plain text comparison)
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Check if user is active
        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        return user;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}