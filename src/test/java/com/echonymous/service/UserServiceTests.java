package com.echonymous.service;

import com.echonymous.dto.LoginDTO;
import com.echonymous.dto.UserDTO;
import com.echonymous.entity.User;
import com.echonymous.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // This ensures Mockito annotations are processed
class UserServiceTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    private UserDTO userDTO;
    private LoginDTO loginDTO;
    private User user;

    @BeforeEach
    public void setup() {
        userDTO = new UserDTO("testUser@gmail.com", "testUser", "testPassword");
        loginDTO = new LoginDTO("testUser", "testPassword");

        user = new User();
        user.setEmail("testUser@gmail.com");
        user.setUsername("testUser");
        user.setPassword("encodedTestPassword");
    }

    @Test
    public void testSignup_ThrowExceptionUsernameAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.of(new User()));

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.signup(userDTO));
        assertEquals("Username already exists.", exception.getMessage());
    }

    @Test
    public void testSignup_ThrowExceptionEmailAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(new User()));

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.signup(userDTO));
        assertEquals("Email already exists.", exception.getMessage());
    }

    @Test
    public void testSignup_Success() {
        // Arrange: Mock the repository to return empty for both username and email checks.
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedTestPassword");

        // Act: Call the signup method.
        userService.signup(userDTO);

        // Assert: Verify that the userRepository's save method was called.
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testLogin_ReturnsSuccess() {
        // Arrange: Mock repository to return user and mock password matching.
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())).thenReturn(true);

        // Act
        boolean isLoginSuccess = userService.login(loginDTO);

        // Assert: Verify login is successful.
        assertTrue(isLoginSuccess);
    }

    @Test
    public void testLogin_InvalidUsername_ReturnsFalse() {
        // Arrange: Mock repository to return empty for username lookup.
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.empty());

        // Act
        boolean isLoginSuccess = userService.login(loginDTO);

        // Assert: Verify login fails.
        assertFalse(isLoginSuccess);
    }

    @Test
    public void testLogin_InvalidPassword_ReturnsFalse() {
        // Arrange: Mock repository to return user and mock password not matching.
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())).thenReturn(false);

        // Act
        boolean isLoginSuccess = userService.login(loginDTO);

        // Assert: Verify login fails.
        assertFalse(isLoginSuccess);
    }
}