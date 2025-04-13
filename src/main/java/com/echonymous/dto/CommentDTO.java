package com.echonymous.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int commentLikesCount;
    private boolean isCommentLiked;
    private Long parentCommentId; // null for top-level comments
    private int replyCount;
    private Boolean isCurrentUserComment; // flag to indicate if the authenticated user authored this comment

}
