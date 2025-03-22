package com.echonymous.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ToggleLikeResultDTO {
    private boolean isLiked;    // true for like, false for dislike
    private int likeCount;
}
