package com.echonymous.controller;

import com.echonymous.dto.ApiResponseDTO;
import com.echonymous.dto.LoginDTO;
import com.echonymous.dto.UserDTO;
import com.echonymous.entity.User;
import com.echonymous.service.UserService;
import com.echonymous.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTests {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private AuthController authController;

    private UserDTO validUserDTO;
    private LoginDTO validLoginDTO;
    private User user;

    @BeforeEach
    void setUp() {
        validUserDTO = new UserDTO("newUser@gmail.com", "newUser", "validPassword123");
        validLoginDTO = new LoginDTO("newUser", "validPassword123");

        user = new User();
        user.setUserId(1L);
        user.setUsername(validUserDTO.getUsername());
        user.setEmail(validUserDTO.getEmail());
        user.setPassword(validUserDTO.getPassword());
    }

    @Test
    void testSignup_ShouldReturnSuccess_WhenValidUserDTO() {
        when(userService.signup(validUserDTO)).thenReturn(user);
        when(jwtUtils.generateToken(user.getUserId())).thenReturn("jwtToken");

        ResponseEntity<ApiResponseDTO> response = authController.signup(validUserDTO, bindingResult);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Sign up successful.", response.getBody().getDetails());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());
    }

    @Test
    void testSignup_ShouldReturnValidationError_WhenBindingResultHasErrors() {
        // We need to make sure BindingResult has errors
        when(bindingResult.hasErrors()).thenReturn(true);

        ResponseEntity<ApiResponseDTO> response = authController.signup(validUserDTO, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getDetails().contains("Validation failed"));
    }

    @Test
    void testLogin_ShouldReturnSuccess_WhenValidLoginDTO() {
        when(userService.login(validLoginDTO)).thenReturn(true);
        when(jwtUtils.generateToken(user.getUserId())).thenReturn("jwtToken");
        when(userService.findByUsername(validLoginDTO.getUsername())).thenReturn(user);

        ResponseEntity<ApiResponseDTO> response = authController.login(validLoginDTO, bindingResult);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Login successful.", response.getBody().getDetails());
        assertNotNull(response.getBody().getToken());
        assertNotNull(response.getBody().getUser());
    }

    @Test
    void testLogin_ShouldReturnFailure_WhenLoginFails() {
        when(userService.login(validLoginDTO)).thenReturn(false);

        ResponseEntity<ApiResponseDTO> response = authController.login(validLoginDTO, bindingResult);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid username or password.", response.getBody().getDetails());
        assertNull(response.getBody().getToken());
        assertNull(response.getBody().getUser());
    }

    @Test
    void testLogin_ShouldReturnValidationError_WhenBindingResultHasErrors() {
        // We need to make sure BindingResult has errors
        when(bindingResult.hasErrors()).thenReturn(true);

        ResponseEntity<ApiResponseDTO> response = authController.login(validLoginDTO, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getDetails().contains("Validation failed"));
    }
}
