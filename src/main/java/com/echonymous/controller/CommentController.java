package com.echonymous.controller;

import com.echonymous.dto.ApiResponseDTO;
import com.echonymous.dto.CommentDTO;
import com.echonymous.dto.FeedResponseDTO;
import com.echonymous.service.CommentService;
import com.echonymous.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;
    private final JwtUtils jwtUtils;

    public CommentController(CommentService commentService, JwtUtils jwtUtils) {
        this.commentService = commentService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<ApiResponseDTO> createComment(@PathVariable Long postId,
                                                        @RequestParam(required = false) Long parentCommentId,
                                                        @RequestBody Map<String, String> payload,
                                                        HttpServletRequest request) {
        String token = jwtUtils.extractJwtFromRequest(request);
        if (token == null || !jwtUtils.validateToken(token)) {
            log.error("Invalid or missing JWT token.");
            return ResponseEntity.status(401).body(
                    new ApiResponseDTO(401, false, "Invalid or missing JWT token.")
            );
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        log.debug("Authenticated user id: {}", userId);

        String comment = payload.get("comment");
        CommentDTO commentDTO = commentService.createComment(postId, userId, comment, parentCommentId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("comment", commentDTO);
        ApiResponseDTO response = new ApiResponseDTO(200, true, "Comment created successfully.", responseData);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponseDTO> updateComment(@PathVariable Long commentId,
                                                        @RequestBody Map<String, String> payload,
                                                        HttpServletRequest request) {
        String token = jwtUtils.extractJwtFromRequest(request);
        if(token == null || !jwtUtils.validateToken(token)){
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO(401, false, "Invalid or missing JWT token."));
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        String newComment = payload.get("comment");
        CommentDTO commentDTO = commentService.updateComment(commentId, userId, newComment);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("comment", commentDTO);

        ApiResponseDTO response = new ApiResponseDTO(200, true, "Comment updated successfully.", null, responseData);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponseDTO> deleteComment(@PathVariable Long commentId,
                                                        HttpServletRequest request) {
        String token = jwtUtils.extractJwtFromRequest(request);
        if(token == null || !jwtUtils.validateToken(token)){
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO(401, false, "Invalid or missing JWT token."));
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        commentService.deleteComment(commentId, userId);
        ApiResponseDTO response = new ApiResponseDTO(200, true, "Comment deleted successfully.");
        return ResponseEntity.ok(response);
    }

    // Fetch top-level comments for a post (with pagination)
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponseDTO> getCommentsForPost(@PathVariable Long postId,
                                                             @RequestParam(required = false) String cursor,
                                                             @RequestParam(defaultValue = "10") int limit,
                                                             HttpServletRequest request) {
        String token = jwtUtils.extractJwtFromRequest(request);
        if(token == null || !jwtUtils.validateToken(token)){
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO(401, false, "Invalid or missing JWT token."));
        }

        Long currentUserId = jwtUtils.getUserIdFromToken(token);
        FeedResponseDTO<CommentDTO> comments = commentService.getCommentsForPost(postId, cursor, limit, currentUserId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("comments", comments);
        ApiResponseDTO response = new ApiResponseDTO(200, true,
                "Comments fetched successfully.", responseData);
        return ResponseEntity.ok(response);
    }

    // Fetch replies for a comment (with pagination)
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponseDTO> getRepliesForComment(@PathVariable Long commentId,
                                                               @RequestParam(required = false) String cursor,
                                                               @RequestParam(defaultValue = "10") int limit,
                                                               HttpServletRequest request) {
        String token = jwtUtils.extractJwtFromRequest(request);
        if(token == null || !jwtUtils.validateToken(token)){
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO(401, false, "Invalid or missing JWT token."));
        }
        Long currentUserId = jwtUtils.getUserIdFromToken(token);
        FeedResponseDTO<CommentDTO> replies = commentService.getRepliesForComment(commentId, cursor, limit, currentUserId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("replies", replies);
        ApiResponseDTO response = new ApiResponseDTO(200, true,
                "Replies fetched successfully.", responseData);
        return ResponseEntity.ok(response);
    }

    // Toggle like on a comment
    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponseDTO> toggleCommentLike(@PathVariable Long commentId,
                                                            HttpServletRequest request) {
        String token = jwtUtils.extractJwtFromRequest(request);
        if(token == null || !jwtUtils.validateToken(token)){
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO(401, false, "Invalid or missing JWT token."));
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        CommentDTO commentDTO = commentService.toggleCommentLike(commentId, userId);
        String message = commentDTO.isCommentLiked() ? "Liked comment successfully" : "Disliked comment successfully";
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("comment", commentDTO);
        ApiResponseDTO response = new ApiResponseDTO(200, true, message,  responseData);
        return ResponseEntity.ok(response);
    }
}
