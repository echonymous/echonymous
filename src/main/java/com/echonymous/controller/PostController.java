package com.echonymous.controller;

import com.echonymous.dto.*;
import com.echonymous.entity.Post;
import com.echonymous.service.PostService;
import com.echonymous.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final JwtUtils jwtUtils;

    public PostController(PostService postService, JwtUtils jwtUtils) {
        this.postService = postService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/upload-text")
    public ResponseEntity<ApiResponseDTO> uploadTextPost(
            @RequestBody PostRequestDTO postRequestDTO, HttpServletRequest request) {

        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401).body(
                   new ApiResponseDTO(401, false, "Invalid or missing JWT token.")
            );
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        log.debug("Authenticated user id: {}", userId);

        Post post = postService.createTextPost(postRequestDTO.getCategory(), postRequestDTO.getContent(), userId);
        log.info("Post successfully created for user: {}", userId);

        ApiResponseDTO response = new ApiResponseDTO(200, true, "Upload successful.");
        return ResponseEntity.ok(response);
    }

    // Future endpoint for uploading audio posts
    @PostMapping("/upload-audio")
    public ResponseEntity<ApiResponseDTO> uploadAudioPost(
            @RequestParam String category, @RequestParam String filePath, HttpServletRequest request) {

        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401).body(
                    new ApiResponseDTO(401, false, "Invalid or missing JWT token.")
            );
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        log.debug("Authenticated user id: {}", userId);

        Post post = postService.createAudioPost(category, filePath, userId);
        log.info("Audio post successfully created for user: {}", userId);

        ApiResponseDTO response = new ApiResponseDTO(200, true, "Upload successful.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/text-feed")
    public ResponseEntity<ApiResponseDTO> getTextFeed(
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401).body(
                    new ApiResponseDTO(401, false, "Invalid or missing JWT token.")
            );
        }

        FeedResponseDTO<TextPostDTO> feed = postService.getTextFeed(cursor, limit);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("feed", feed);

        ApiResponseDTO response = new ApiResponseDTO(200, true, "Text feed fetched successfully.",
                null, responseData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponseDTO> toggleLike(
            @PathVariable Long postId, HttpServletRequest request) {

        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401).body(
                    new ApiResponseDTO(401, false, "Invalid or missing JWT token.")
            );
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        ToggleLikeResultDTO result = postService.toggleLike(postId, userId);
        String details = result.isLiked() ? "Liked successfully" : "Disliked successfully";

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("likeCount", result.getLikeCount());

        ApiResponseDTO response = new ApiResponseDTO(200, true, details,
                null, responseData);

        return ResponseEntity.ok(response);
    }

}
