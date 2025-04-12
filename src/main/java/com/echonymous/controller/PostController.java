package com.echonymous.controller;

import com.echonymous.dto.*;
import com.echonymous.entity.Post;
import com.echonymous.entity.User;
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
@RequestMapping("/api/posts")
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
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "All") String category,
            HttpServletRequest request) {

        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401).body(
                    new ApiResponseDTO(401, false, "Invalid or missing JWT token.")
            );
        }

        Long currentUserId = jwtUtils.getUserIdFromToken(token);
        FeedResponseDTO<TextPostDTO> feed = postService.getTextFeed(cursor, limit, currentUserId, category);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("feed", feed);

        ApiResponseDTO response = new ApiResponseDTO(200, true, "Text feed fetched successfully.",
                responseData);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/text-feed/{id}")
    public ResponseEntity<ApiResponseDTO> getTextPostById(
            @PathVariable Long id, HttpServletRequest request) {

        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401).body(
                    new ApiResponseDTO(401, false, "Invalid or missing JWT token.")
            );
        }

        Long currentUserId = jwtUtils.getUserIdFromToken(token);
        TextPostDTO textPostDTO = postService.getTextPostById(id, currentUserId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("textPost", textPostDTO);

        ApiResponseDTO response = new ApiResponseDTO(200, true, "Text post fetched successfully.", responseData);
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
        responseData.put("likesCount", result.getLikesCount());

        ApiResponseDTO response = new ApiResponseDTO(200, true, details, responseData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/echo")
    public ResponseEntity<ApiResponseDTO> toggleEcho(
            @PathVariable Long postId, HttpServletRequest request) {

        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401).body(
                    new ApiResponseDTO(401, false, "Invalid or missing JWT token.")
            );
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        ToggleEchoResultDTO result = postService.toggleEcho(postId, userId);
        String details = result.isEchoed() ? "Echoed successfully" : "Unechoed successfully";

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("echoesCount", result.getEchoesCount());

        ApiResponseDTO response = new ApiResponseDTO(200, true, details, responseData);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/echoed")
    public ResponseEntity<ApiResponseDTO> getEchoedTextPosts(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401).body(
                    new ApiResponseDTO(401, false, "Invalid or missing JWT token.")
            );
        }

        Long userId = jwtUtils.getUserIdFromToken(token);

        FeedResponseDTO<TextPostDTO> feed = postService.getEchoedTextPosts(userId, limit, cursor);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("echoedPosts", feed);
        ApiResponseDTO response = new ApiResponseDTO(200, true, "Echoed text posts fetched successfully.", responseData);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-feed")
    public ResponseEntity<ApiResponseDTO> getMyTextPosts(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO(401, false, "Invalid or missing JWT token."));
        }

        Long currentUserId = jwtUtils.getUserIdFromToken(token);
        Long targetUserId = (userId == null) ? currentUserId : userId;
        FeedResponseDTO<TextPostDTO> feed = postService.getUserTextPosts(cursor, limit, targetUserId, currentUserId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("myTextPosts", feed);

        ApiResponseDTO response = new ApiResponseDTO(200, true, "My text posts fetched successfully.", responseData);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit-text-feed/{postId}")
    public ResponseEntity<ApiResponseDTO> editTextPost(
            @PathVariable Long postId,
            @RequestBody PostRequestDTO postRequestDTO,
            HttpServletRequest request) {

        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO(401, false, "Invalid or missing JWT token."));
        }
        Long currentUserId = jwtUtils.getUserIdFromToken(token);

        TextPostDTO updatedPost = postService.updateTextPost(
                postId,
                currentUserId,
                postRequestDTO.getCategory(),
                postRequestDTO.getContent()
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("updatedPost", updatedPost);

        ApiResponseDTO response = new ApiResponseDTO(200, true, "Post updated successfully.", responseData);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-text-feed/{postId}")
    public ResponseEntity<ApiResponseDTO> deleteTextPost(@PathVariable Long postId,
                                                     HttpServletRequest request) {
        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO(401, false, "Invalid or missing JWT token."));
        }
        Long currentUserId = jwtUtils.getUserIdFromToken(token);
        postService.deletePost(postId, currentUserId);
        ApiResponseDTO response = new ApiResponseDTO(200, true, "Post deleted successfully.");
        return ResponseEntity.ok(response);
    }

}
