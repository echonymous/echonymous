package com.echonymous.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTests {

    private JwtUtils jwtUtils;

    private Long userId = 2L;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        jwtUtils = new JwtUtils();

        // Use reflection to inject values into private fields
        Field secretKeyField = JwtUtils.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtUtils, "88cd2108b5347d973cf39cdf9053d7dd42704876d8c9a9bd8e2d168259d3ddf7");

        Field expirationTimeField = JwtUtils.class.getDeclaredField("expirationTime");
        expirationTimeField.setAccessible(true);
        expirationTimeField.set(jwtUtils, 3600000L);  // 1 hour
    }

    @Test
    public void testGenerateToken_ShouldGenerateValidToken_WhenUserIdIsProvided() {
        String token = jwtUtils.generateToken(userId);
        assertNotNull(token);
        assertTrue(token.startsWith("eyJ")); // Check if the token starts with "eyJ" (standard JWT token format)
    }

    @Test
    public void testValidateToken_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtUtils.generateToken(userId);
        boolean isValid = jwtUtils.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token";
        boolean isValid = jwtUtils.validateToken(invalidToken);
        assertFalse(isValid);
    }

    @Test
    public void testGetUserIdFromToken_ShouldReturnCorrectUserId_WhenTokenIsValid() {
        String token = jwtUtils.generateToken(userId);
        Long extractedUserId = jwtUtils.getUserIdFromToken(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    public void testGetUserIdFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token";
        assertThrows(io.jsonwebtoken.MalformedJwtException.class, () -> {
            jwtUtils.getUserIdFromToken(invalidToken);
        });
    }

    @Test
    public void testExtractJwtFromRequest_ShouldReturnToken_WhenAuthorizationHeaderContainsBearerToken() {
        // Simulate a HttpServletRequest with an Authorization header
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        String bearerToken = "Bearer " + jwtUtils.generateToken(userId);
        org.mockito.Mockito.when(request.getHeader("Authorization")).thenReturn(bearerToken);

        String token = jwtUtils.extractJwtFromRequest(request);
        assertNotNull(token);
        assertTrue(token.startsWith("eyJ"));
    }

    @Test
    public void testExtractJwtFromRequest_ShouldReturnNull_WhenAuthorizationHeaderIsMissing() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        org.mockito.Mockito.when(request.getHeader("Authorization")).thenReturn(null);

        String token = jwtUtils.extractJwtFromRequest(request);
        assertNull(token);
    }

    @Test
    public void testExtractJwtFromRequest_ShouldReturnNull_WhenAuthorizationHeaderDoesNotStartWithBearer() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        org.mockito.Mockito.when(request.getHeader("Authorization")).thenReturn("Basic some_token");

        String token = jwtUtils.extractJwtFromRequest(request);
        assertNull(token);
    }
}
