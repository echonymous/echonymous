package com.echonymous.controller;

import com.echonymous.dto.ApiResponseDTO;
import com.echonymous.dto.LoginDTO;
import com.echonymous.dto.UserDTO;
import com.echonymous.entity.User;
import com.echonymous.service.UserService;
import com.echonymous.util.JwtUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponseDTO> signup(@Valid @RequestBody UserDTO userDTO, BindingResult bindingResult) {
        // If there are any validation err (empty fields, invalid email, password too short), BindingResult captures them
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(400).body(
                    new ApiResponseDTO(400, false, "Validation failed. Check input fields.")
            );
        }

        log.info("Signing up user: {}", userDTO.getUsername());
        User user = userService.signup(userDTO);

        String token = jwtUtils.generateToken(user.getId());
        log.info("JWT Token: {}", token);

        Map<String, Object> userMap = Map.of(
                "id", user.getId(),
                "username", user.getUsername()
        );

        ApiResponseDTO response = new ApiResponseDTO(200, true, "Sign up successful.",
                token, userMap);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(400).body(
                    new ApiResponseDTO(400, false, "Validation failed. Check input fields.")
            );
        }

        log.info("Attempting login for user: {}", loginDTO.getUsername());

        boolean isLoginSuccess = userService.login(loginDTO);

        if (isLoginSuccess) {
            log.info("Login successful for user: {}", loginDTO.getUsername());

            User user = userService.findByUsername(loginDTO.getUsername());

            String token = jwtUtils.generateToken(user.getId());
            log.info("JWT Token: {}", token);

            Map<String, Object> userMap = Map.of(
                    "id", user.getId(),
                    "username", user.getUsername()
            );

            return ResponseEntity.ok(
                    new ApiResponseDTO(200, true, "Login successful.", token, userMap)
            );
        } else {
            log.error("Login failed for user: {}", loginDTO.getUsername());
            return ResponseEntity.status(401).body(
                    new ApiResponseDTO(401, false, "Invalid username or password.")
            );
        }
    }
}
