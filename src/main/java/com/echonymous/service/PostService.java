package com.echonymous.service;

import com.echonymous.dto.EngagementDTO;
import com.echonymous.dto.FeedResponseDTO;
import com.echonymous.dto.TextPostDTO;
import com.echonymous.dto.ToggleLikeResultDTO;
import com.echonymous.entity.*;
import com.echonymous.repository.PostLikeRepository;
import com.echonymous.repository.PostRepository;
import com.echonymous.repository.TextPostRepository;
import com.echonymous.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final TextPostRepository textPostRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, TextPostRepository textPostRepository, PostLikeRepository postLikeRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.textPostRepository = textPostRepository;
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
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

    public FeedResponseDTO<TextPostDTO> getTextFeed(String cursor, int limit, Long currentUserId) {
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

        List<TextPostDTO> postDTOs = posts.stream().map(post -> {
            int likesCount = postLikeRepository.countByPost(post);
            boolean isLiked = postLikeRepository.findByPostAndUser_UserId(post, currentUserId).isPresent();
            // For now, commentsCount and isEchoed are not implemented
            EngagementDTO engagement = new EngagementDTO(likesCount, 0, isLiked, false);

            return new TextPostDTO(
                    post.getPostId(),
                    post.getCategory(),
                    post.getContent(),
                    post.getCreatedAt(),
                    engagement
            );
        }).collect(Collectors.toList());

        return new FeedResponseDTO<>(postDTOs, nextCursor, hasNext);
    }

    @Transactional
    public ToggleLikeResultDTO toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        boolean isLiked;

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);
        if (existingLike.isPresent()) {
            // If the like exists, delete it (i.e. user unlikes)
            postLikeRepository.delete(existingLike.get());
            isLiked = false;
            log.info("User {} unliked post {}", userId, postId);
        } else {
            // If no like exists, create a new like record
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(user);
            like.setLikedAt(LocalDateTime.now());
            postLikeRepository.save(like);
            isLiked = true;
            log.info("User {} liked post {}", userId, postId);
        }
        // Return the updated like count and the like action
        int updatedLikeCount = postLikeRepository.countByPost(post);
        return new ToggleLikeResultDTO(isLiked, updatedLikeCount);
    }
}
