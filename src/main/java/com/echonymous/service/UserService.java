package com.echonymous.service;

import com.echonymous.dto.LoginDTO;
import com.echonymous.dto.UserRequestDTO;
import com.echonymous.entity.User;
import com.echonymous.exception.NotFoundException;
import com.echonymous.repository.UserRepository;
import jakarta.validation.ValidationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(UserRequestDTO userRequestDTO) {
        Optional<User> existingUserByUsername = userRepository.findByUsername(userRequestDTO.getUsername());
        if (existingUserByUsername.isPresent()) {
            throw new ValidationException("Username already exists.");
        }

        Optional<User> existingUserByEmail = userRepository.findByEmail(userRequestDTO.getEmail());
        if (existingUserByEmail.isPresent()) {
            throw new ValidationException("Email already exists.");
        }

        User user = new User();
        user.setEmail(userRequestDTO.getEmail());
        user.setUsername(userRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));

        userRepository.save(user);
        return user;
    }

    public boolean login(LoginDTO loginDTO) {
        Optional<User> userOpt = userRepository.findByUsername(loginDTO.getUsername());

        if (userOpt.isEmpty()) {
            throw new NotFoundException("User not found.");
        }

        User user = userOpt.get();
        return passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }
}
