package com.echonymous.controller;

import com.echonymous.dto.ApiResponseDTO;
import com.echonymous.dto.PostRequestDTO;
import com.echonymous.entity.TextPost;
import com.echonymous.service.PostService;
import com.echonymous.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostControllerTests {

    @Mock
    private PostService postService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PostController postController;

    private PostRequestDTO validPostRequestDTO;

    private TextPost textPost;

    private String validToken;
    private Long userId;

    @BeforeEach
    void setUp() {
        validPostRequestDTO = new PostRequestDTO("Test", "This is a test post.");
        userId = 1L;

        textPost = new TextPost();
        textPost.setPostId(1L);
        textPost.setCategory("Test");
        textPost.setContent("This is a test post.");
        textPost.setAuthorId(userId);

        validToken = "valid-jwt-token";
    }

    @Test
    void testUploadTextPost_ShouldReturnSuccess_WhenValidRequest() {
        // Arrange
        when(jwtUtils.extractJwtFromRequest(request)).thenReturn(validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(validToken)).thenReturn(userId);
        when(postService.createTextPost(validPostRequestDTO.getCategory(), validPostRequestDTO.getContent(), userId)).thenReturn(textPost);

        // Act
        ResponseEntity<ApiResponseDTO> response = postController.uploadTextPost(validPostRequestDTO, request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Upload successful.", response.getBody().getDetails());
    }

    @Test
    void testUploadTextPost_ShouldReturnUnauthorized_WhenTokenIsInvalid() {
        // Arrange
        when(jwtUtils.extractJwtFromRequest(request)).thenReturn(validToken);
        when(jwtUtils.validateToken(validToken)).thenReturn(false);

        // Act
        ResponseEntity<ApiResponseDTO> response = postController.uploadTextPost(validPostRequestDTO, request);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid or missing JWT token.", response.getBody().getDetails());
    }

    @Test
    void testUploadTextPost_ShouldReturnUnauthorized_WhenTokenIsMissing() {
        // Arrange
        when(jwtUtils.extractJwtFromRequest(request)).thenReturn(null);

        // Act
        ResponseEntity<ApiResponseDTO> response = postController.uploadTextPost(validPostRequestDTO, request);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid or missing JWT token.", response.getBody().getDetails());
    }
}
