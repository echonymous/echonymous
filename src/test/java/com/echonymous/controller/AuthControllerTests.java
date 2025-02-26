package com.echonymous.controller;

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
import org.springframework.validation.ObjectError;

import java.util.Collections;
import java.util.Map;

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

    // We are excluding test of the private method validationErrorResponse() and assuming DTOs passed are valid

    @BeforeEach
    void setUp() {
        validUserDTO = new UserDTO("newUser@gmail.com", "newUser", "validPassword123");
        validLoginDTO = new LoginDTO("newUser", "validPassword123");

        user = new User();
        user.setId(1L);
        user.setUsername(validUserDTO.getUsername());
        user.setEmail(validUserDTO.getEmail());
        user.setPassword(validUserDTO.getPassword());
    }

    @Test
    void testSignup_Success() {
        when(userService.signup(validUserDTO)).thenReturn(user);
        when(jwtUtils.generateToken(user.getUsername())).thenReturn("jwtToken");

        ResponseEntity<Map<String, Object>> response = authController.signup(validUserDTO, bindingResult);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Sign up successful.", response.getBody().get("details"));
        assertNotNull(response.getBody().get("token"));
        assertNotNull(response.getBody().get("user"));
    }

    @Test
    void testSignup_ValidationError() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(new ObjectError("field",
                "error")));

        ResponseEntity<Map<String, Object>> response = authController.signup(validUserDTO, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("details").toString().contains("Validation failed"));
    }

    @Test
    void testLogin_Success() {
        when(userService.login(validLoginDTO)).thenReturn(true);
        when(jwtUtils.generateToken(validLoginDTO.getUsername())).thenReturn("jwtToken");
        when(userService.findByUsername(validLoginDTO.getUsername())).thenReturn(user);

        ResponseEntity<Map<String, Object>> response = authController.login(validLoginDTO, bindingResult);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Login successful.", response.getBody().get("details"));
        assertNotNull(response.getBody().get("token"));
        assertNotNull(response.getBody().get("user"));
    }

    @Test
    void testLogin_Failure() {
        when(userService.login(validLoginDTO)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = authController.login(validLoginDTO, bindingResult);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Invalid username or password.", response.getBody().get("details"));
        assertNull(response.getBody().get("token"));
        assertNull(response.getBody().get("user"));
    }

    @Test
    void testLogin_ValidationError() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(new ObjectError("field",
                "error")));

        ResponseEntity<Map<String, Object>> response = authController.login(validLoginDTO, bindingResult);

        assertEquals(400, response.getStatusCodeValue());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("details").toString().contains("Validation failed"));
    }
}
