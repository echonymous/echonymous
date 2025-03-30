package com.echonymous.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedResponseDTO<T> {
    private List<T> content; // Using it for posts, comments, replies
    private String nextCursor;
    private boolean hasNext;
}
