package com.echonymous.service;

import com.echonymous.entity.TextPost;
import com.echonymous.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTests {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private TextPost validTextPost;
    private Long userId;
    private String category;
    private String content;

    @BeforeEach
    void setUp() {
        userId = 1L;
        category = "Test";
        content = "This is a test post.";

        validTextPost = new TextPost();
        validTextPost.setCategory(category);
        validTextPost.setContent(content);
        validTextPost.setAuthorId(userId);
        validTextPost.setCreatedAt(LocalDateTime.now());
        validTextPost.setPostId(1L);
    }

    @Test
    void testCreateTextPost_ShouldSavePost_WhenValidData() {
        // Arrange
        when(postRepository.save(any(TextPost.class))).thenReturn(validTextPost);

        // Act
        TextPost savedPost = (TextPost) postService.createTextPost(category, content, userId);

        // Assert
        assertNotNull(savedPost);
        assertEquals(validTextPost.getCategory(), savedPost.getCategory());
        assertEquals(validTextPost.getContent(), savedPost.getContent());
        assertEquals(validTextPost.getAuthorId(), savedPost.getAuthorId());
        assertNotNull(savedPost.getCreatedAt());
        assertEquals(1L, savedPost.getPostId());
        verify(postRepository, times(1)).save(any(TextPost.class));
    }

    @Test
    void testCreateTextPost_ShouldThrowException_WhenPostCreationFails() {
        // Arrange
        when(postRepository.save(any(TextPost.class))).thenThrow(new EntityNotFoundException("Post not found."));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> postService.createTextPost(category, content, userId));
        verify(postRepository, times(1)).save(any(TextPost.class));
    }
}
