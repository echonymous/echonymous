package com.echonymous.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EngagementDTO {
    private int likesCount;
    private int commentsCount;
    private int echoesCount;
    private boolean isLiked;
    private boolean isEchoed;

}
