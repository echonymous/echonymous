package com.echonymous.service;

import com.echonymous.dto.FeedResponseDTO;
import com.echonymous.dto.TextPostDTO;
import com.echonymous.entity.AudioPost;
import com.echonymous.entity.Post;
import com.echonymous.entity.TextPost;
import com.echonymous.repository.PostRepository;
import com.echonymous.repository.TextPostRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final TextPostRepository textPostRepository;

    public PostService(PostRepository postRepository, TextPostRepository textPostRepository) {
        this.postRepository = postRepository;
        this.textPostRepository = textPostRepository;
    }

    @Transactional
    public Post createTextPost(String category, String content, Long userId) {
        TextPost post = new TextPost();
        post.setCategory(category);
        post.setContent(content);
        post.setAuthorId(userId);
        post.setCreatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    // Future method for handling audio posts (once implemented)
    @Transactional
    public Post createAudioPost(String category, String filePath, Long userId) {
        AudioPost post = new AudioPost();
        post.setCategory(category);
        post.setFilePath(filePath);
        post.setAuthorId(userId);
        post.setCreatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public FeedResponseDTO<TextPostDTO> getTextFeed(String cursor, int limit) {
        LocalDateTime cursorDate = null;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                // Expecting the cursor in ISO_LOCAL_DATE_TIME format, e.g. "2025-03-07T15:30:00"
                cursorDate = LocalDateTime.parse(cursor);
            } catch (DateTimeParseException e) {
                throw new ValidationException("Invalid cursor format. Expected ISO_LOCAL_DATE_TIME.");
            }
        }
        // Request one extra record to determine if there's a next page
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<TextPost> posts;
        if (cursorDate != null) {
            posts = textPostRepository.findByCreatedAtBeforeOrderByCreatedAtDesc(cursorDate, pageable);
        } else {
            posts = textPostRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        boolean hasNext = posts.size() > limit;
        if (hasNext) {
            posts = posts.subList(0, limit);
        }
        // Next cursor is the createdAt of the last post in the list
        String nextCursor = null;
        if (!posts.isEmpty()) {
            LocalDateTime lastCreatedAt = posts.get(posts.size() - 1).getCreatedAt();
            nextCursor = lastCreatedAt.toString();
        }

        List<TextPostDTO> postDTOs = posts.stream()
                .map(post -> new TextPostDTO(post.getPostId(), post.getCategory(), post.getContent(), post.getCreatedAt()))
                .collect(Collectors.toList());

        return new FeedResponseDTO<>(postDTOs, nextCursor, hasNext);
    }
}
