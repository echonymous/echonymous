package com.echonymous.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TextPostDTO {
    private Long postId;
    private String category;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private EngagementDTO engagement;
    private Boolean isCurrentUserPost; // flag to indicate if the authenticated user authored this post

}
