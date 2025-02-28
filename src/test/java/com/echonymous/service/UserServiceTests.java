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

@ExtendWith(MockitoExtension.class)
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
        userDTO = new UserDTO("testUser@gmail.com", "testUser", "testPassword123");
        loginDTO = new LoginDTO("testUser", "testPassword123");

        user = new User();
        user.setEmail("testUser@gmail.com");
        user.setUsername("testUser");
        user.setPassword("encodedTestPassword123");
    }

    @Test
    public void testSignup_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.of(new User()));

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.signup(userDTO));
        assertEquals("Username already exists.", exception.getMessage());
    }

    @Test
    public void testSignup_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.signup(userDTO));
        assertEquals("Email already exists.", exception.getMessage());
    }

    @Test
    public void testSignup_ShouldSignupSuccessfully_WhenUsernameAndEmailAreAvailable() {
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedTestPassword");

        userService.signup(userDTO);

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

        boolean isLoginSuccess = userService.login(loginDTO);

        assertFalse(isLoginSuccess);
    }

    @Test
    public void testLogin_ShouldReturnFalse_WhenPasswordIsInvalid() {
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())).thenReturn(false);

        boolean isLoginSuccess = userService.login(loginDTO);

        assertFalse(isLoginSuccess);
    }

    @Test
    public void testLogin_ShouldReturnFalse_WhenCredentialsAreNull() {
        LoginDTO nullLoginDTO = new LoginDTO(null, null);

        boolean isLoginSuccess = userService.login(nullLoginDTO);

        assertFalse(isLoginSuccess);
    }

    @Test
    public void testLogin_ShouldReturnFalse_WhenInputFieldsAreEmpty() {
        LoginDTO emptyLoginDTO = new LoginDTO("", "");

        boolean isLoginSuccess = userService.login(emptyLoginDTO);

        assertFalse(isLoginSuccess);
    }

    @Test
    public void testFindByUsername_ShouldReturnUser_WhenUsernameExists() {
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.of(user));

        User foundUser = userService.findByUsername(userDTO.getUsername());

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
    }

    @Test
    public void testFindByUsername_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername(userDTO.getUsername())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.findByUsername(userDTO.getUsername()));
        assertEquals("User not found.", exception.getMessage());
    }
}

