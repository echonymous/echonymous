package com.echonymous.controller;

import com.echonymous.dto.LoginDTO;
import com.echonymous.dto.UserDTO;
import com.echonymous.entity.User;
import com.echonymous.service.UserService;
import com.echonymous.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        // If there are any validation err (empty fields, invalid email, password too short), BindingResult captures them
        if (bindingResult.hasErrors()) {
            return validationErrorResponse(bindingResult);
        }

        try {
            log.info("Signing up user: {}", userDTO.getUsername());
            User user = userService.signup(userDTO);

            String token = jwtUtils.generateToken(user.getUsername());
            log.info("JWT Token: {}", token);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("success", true);
            response.put("details", "Sign up successful.");
            response.put("token", token);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername()
                    // Add other user data fields like role later
            ));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 400);
            response.put("success", false);
            response.put("details", e.getMessage());
            response.put("token", null);
            response.put("user", null);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return validationErrorResponse(bindingResult);
        }

        log.info("Attempting login for user: {}", loginDTO.getUsername());

        boolean isLoginSuccess = userService.login(loginDTO);

        Map<String, Object> response = new HashMap<>();

        if (isLoginSuccess) {
            log.info("Login successful for user: {}", loginDTO.getUsername());

            String token = jwtUtils.generateToken(loginDTO.getUsername());
            log.info("JWT Token: {}", token);

            User user = userService.findByUsername(loginDTO.getUsername());

            response.put("status", 200);
            response.put("success", true);
            response.put("details", "Login successful.");
            response.put("token", token);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername()
            ));
        } else {
            log.error("Login failed for user: {}", loginDTO.getUsername());
            response.put("status", 401);
            response.put("success", false);
            response.put("details", "Invalid username or password.");
            response.put("token", null);
            response.put("user", null);
        }

        return ResponseEntity.status(isLoginSuccess ? 200 : 401).body(response);
    }

    private ResponseEntity<Map<String, Object>> validationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("success", false);

        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        for (ObjectError error : bindingResult.getAllErrors()) {
            errorMessage.append(error.getDefaultMessage()).append(" ");
        }

        response.put("details", errorMessage.toString());
        response.put("token", null);
        response.put("user", null);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
