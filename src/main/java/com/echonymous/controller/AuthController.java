package com.echonymous.controller;

import com.echonymous.dto.LoginDTO;
import com.echonymous.dto.UserDTO;
import com.echonymous.service.UserService;
import com.echonymous.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public AuthController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserDTO userDTO) {
        try {
            log.info("Signing up user: {}", userDTO.getUsername());
            userService.signup(userDTO);
            return ResponseEntity.ok("User successfully signed up. Please log in!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("Attempting login for user: {}", loginDTO.getUsername());
        boolean isLoginSuccess = userService.login(loginDTO);

        if (isLoginSuccess) {
            log.info("Login successful for user: {}", loginDTO.getUsername());
            String token = jwtUtils.generateToken(loginDTO.getUsername());
            log.info("JWT Token: {}", token);
            return ResponseEntity.ok(token);
        } else {
            log.error("Login failed for user: {}", loginDTO.getUsername());
            return ResponseEntity.status(401).body("Invalid Credentials!");
        }
    }
}
