package com.echonymous.repository;

import com.echonymous.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setUsername("testUser");
        testUser.setPassword("testPassword");
        userRepository.save(testUser);
    }

    @Test
    public void testFindByUsername_ShouldReturnUser_WhenUsernameExists() {
        Optional<User> user = userRepository.findByUsername("testUser");
        assertTrue(user.isPresent(), "User should be found by username.");
    }

    @Test
    public void testFindByUsername_ShouldReturnEmpty_WhenUsernameDoesNotExist() {
        Optional<User> user = userRepository.findByUsername("nonExistentUser");
        assertFalse(user.isPresent(), "User should not be found when username does not exist.");
    }

    @Test
    public void testFindByEmail_ShouldReturnUser_WhenEmailExists() {
        Optional<User> user = userRepository.findByEmail("testuser@example.com");
        assertTrue(user.isPresent(), "User should be found by email.");
    }

    @Test
    public void testFindByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        Optional<User> user = userRepository.findByEmail("nonexistentuser@example.com");
        assertFalse(user.isPresent(), "User should not be found when email does not exist.");
    }
}
