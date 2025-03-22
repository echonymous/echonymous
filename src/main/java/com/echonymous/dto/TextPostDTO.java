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
public class TextPostDTO {
    private Long postId;
    private String category;
    private String content;
    private LocalDateTime createdAt;
    private int likeCount;

}
