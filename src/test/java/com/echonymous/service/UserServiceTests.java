package com.echonymous.service;

import com.echonymous.dto.LoginDTO;
import com.echonymous.dto.UserRequestDTO;
import com.echonymous.entity.User;
import com.echonymous.exception.NotFoundException;
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

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    private UserRequestDTO userRequestDTO;
    private LoginDTO loginDTO;
    private User user;

    @BeforeEach
    public void setup() {
        userRequestDTO = new UserRequestDTO("testUser@gmail.com", "testUser", "testPassword123");
        loginDTO = new LoginDTO("testUser", "testPassword123");

        user = new User();
        user.setEmail("testUser@gmail.com");
        user.setUsername("testUser");
        user.setPassword("encodedTestPassword123");
    }

    @Test
    public void testSignup_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername(userRequestDTO.getUsername())).thenReturn(Optional.of(new User()));

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.signup(userRequestDTO));
        assertEquals("Username already exists.", exception.getMessage());
    }

    @Test
    public void testSignup_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.findByEmail(userRequestDTO.getEmail())).thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.signup(userRequestDTO));
        assertEquals("Email already exists.", exception.getMessage());
    }

    @Test
    public void testSignup_ShouldSignupSuccessfully_WhenUsernameAndEmailAreAvailable() {
        when(userRepository.findByUsername(userRequestDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userRequestDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userRequestDTO.getPassword())).thenReturn("encodedTestPassword");

        userService.signup(userRequestDTO);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testSignup_ShouldThrowException_WhenInputIsNull() {
        assertThrows(NullPointerException.class, () -> userService.signup(null));
    }

    @Test
    public void testLogin_ShouldReturnTrue_WhenLoginIsSuccessful() {
        // Arrange
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())).thenReturn(true);

        // Act
        boolean isLoginSuccess = userService.login(loginDTO);

        // Assert
        assertTrue(isLoginSuccess);
    }

    @Test
    public void testLogin_ShouldReturnFalse_WhenUsernameIsInvalid() {
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.login(loginDTO));
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    public void testLogin_ShouldReturnFalse_WhenPasswordIsInvalid() {
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())).thenReturn(false);

        boolean isLoginSuccess = userService.login(loginDTO);

        assertFalse(isLoginSuccess);
    }

    @Test
    public void testLogin_ShouldThrowException_WhenCredentialsAreNull() {
        // Assert
        assertThrows(NullPointerException.class, () -> userService.login(null));
    }

    @Test
    public void testFindByUsername_ShouldReturnUser_WhenUsernameExists() {
        when(userRepository.findByUsername(userRequestDTO.getUsername())).thenReturn(Optional.of(user));

        User foundUser = userService.findByUsername(userRequestDTO.getUsername());

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
    }

    @Test
    public void testFindByUsername_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername(userRequestDTO.getUsername())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.findByUsername(userRequestDTO.getUsername()));
        assertEquals("User not found.", exception.getMessage());
    }
}

