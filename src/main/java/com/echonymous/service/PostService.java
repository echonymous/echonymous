package com.echonymous.service;

import com.echonymous.dto.*;
import com.echonymous.entity.*;
import com.echonymous.exception.NotFoundException;
import com.echonymous.repository.*;
import com.echonymous.util.DateTimeUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final TextPostRepository textPostRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostEchoRepository postEchoRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, TextPostRepository textPostRepository, PostLikeRepository postLikeRepository, CommentLikeRepository commentLikeRepository, PostCommentRepository postCommentRepository, PostEchoRepository postEchoRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.textPostRepository = textPostRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCommentRepository = postCommentRepository;
        this.postEchoRepository = postEchoRepository;
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

    public FeedResponseDTO<TextPostDTO> getTextFeed(String cursor, int limit, Long currentUserId, String category) {
        LocalDateTime cursorDate = DateTimeUtils.parseCursor(cursor);

        // Request one extra record to determine if there's a next page
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<TextPost> posts;

        if (!"all".equalsIgnoreCase(category)) {
            if (cursorDate != null) {
                posts = textPostRepository.findByCategoryIgnoreCaseAndCreatedAtBeforeOrderByCreatedAtDesc(category, cursorDate, pageable);
            } else {
                posts = textPostRepository.findByCategoryIgnoreCaseOrderByCreatedAtDesc(category, pageable);
            }
        } else {
            if (cursorDate != null) {
                posts = textPostRepository.findByCreatedAtBeforeOrderByCreatedAtDesc(cursorDate, pageable);
            } else {
                posts = textPostRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        }

        boolean hasNext = posts.size() > limit;
        if (hasNext) {
            posts = posts.subList(0, limit);
        }
        // Next cursor is the createdAt of the last post in the list
        String nextCursor = !posts.isEmpty() ? posts.get(posts.size() - 1).getCreatedAt().toString() : null;

        List<TextPostDTO> postDTOs = posts.stream()
                .map(post -> mapTextPostToDTO(post, currentUserId))
                .collect(Collectors.toList());

        return new FeedResponseDTO<>(postDTOs, nextCursor, hasNext);
    }

    // Generic method to fetch a user's text posts (for both my-feed and other users' feed)
    @Transactional
    public FeedResponseDTO<TextPostDTO> getUserTextPosts(String cursor, int limit, Long targetUserId, Long currentUserId) {
        LocalDateTime cursorDate = DateTimeUtils.parseCursor(cursor);
        // Request limit+1 to check for a next page
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<TextPost> posts;
        if (cursorDate != null) {
            posts = textPostRepository.findByAuthorIdAndCreatedAtBeforeOrderByCreatedAtDesc(targetUserId, cursorDate, pageable);
        } else {
            posts = textPostRepository.findByAuthorIdOrderByCreatedAtDesc(targetUserId, pageable);
        }
        boolean hasNext = posts.size() > limit;
        if (hasNext) {
            posts = posts.subList(0, limit);
        }
        String nextCursor = posts.isEmpty() ? null : posts.get(posts.size() - 1).getCreatedAt().toString();
        List<TextPostDTO> postDTOs = posts.stream()
                .map(post -> mapTextPostToDTO(post, currentUserId))
                .collect(Collectors.toList());

        return new FeedResponseDTO<>(postDTOs, nextCursor, hasNext);
    }

    public TextPostDTO getTextPostById(Long id, Long currentUserId) {
        Optional<TextPost> optionalPost = textPostRepository.findById(id);
        if (!optionalPost.isPresent()) {
            throw new NotFoundException("Text post not found with id: " + id);
        }

        TextPost post = optionalPost.get();
        return mapTextPostToDTO(post, currentUserId);
    }

    @Transactional
    public ToggleLikeResultDTO toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

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

    @Transactional
    public ToggleEchoResultDTO toggleEcho(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        boolean isEchoed;

        Optional<PostEcho> existingEcho = postEchoRepository.findByPostAndUser(post, user);
        if (existingEcho.isPresent()) {
            // If the echo exists, delete it (i.e. user unechoes)
            postEchoRepository.delete(existingEcho.get());
            isEchoed = false;
            log.info("User {} unechoed post {}", userId, postId);
        } else {
            // If no echo exists, create a new echo record
            PostEcho echo = new PostEcho();
            echo.setPost(post);
            echo.setUser(user);
            echo.setEchoedAt(LocalDateTime.now());
            postEchoRepository.save(echo);
            isEchoed = true;
            log.info("User {} echoed post {}", userId, postId);
        }
        // Return the updated echo count and the echo action
        int updatedEchoCount = postEchoRepository.countByPost(post);
        return new ToggleEchoResultDTO(isEchoed, updatedEchoCount);
    }

    public FeedResponseDTO<TextPostDTO> getEchoedTextPosts(Long userId, int limit, String cursor) {
        LocalDateTime cursorDate = DateTimeUtils.parseCursor(cursor);
        // Request one extra record to determine if there's a next page.
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<PostEcho> echoes;
        if (cursorDate != null) {
            echoes = postEchoRepository.findTextPostEchoedByUserBefore(userId, cursorDate, pageable);
        } else {
            echoes = postEchoRepository.findTextPostEchoedByUser(userId, pageable);
        }
        boolean hasNext = echoes.size() > limit;
        if (hasNext) {
            echoes = echoes.subList(0, limit);
        }
        // The next cursor is the echoedAt of the last PostEcho.
        String nextCursor = !echoes.isEmpty() ? echoes.get(echoes.size() - 1).getEchoedAt().toString() : null;

        List<TextPostDTO> postDTOs = echoes.stream().map(echo -> {
            TextPost textPost = (TextPost) echo.getPost();
            return mapTextPostToDTO(textPost, userId);
        }).collect(Collectors.toList());
        return new FeedResponseDTO<>(postDTOs, nextCursor, hasNext);
    }

    /**
     * Maps a TextPost entity to TextPostDTO using engagement metrics.
     */
    private TextPostDTO mapTextPostToDTO(TextPost post, Long currentUserId) {
        int likesCount = postLikeRepository.countByPost(post);
        int commentsCount = postCommentRepository.countByPost(post);
        int echoesCount = postEchoRepository.countByPost(post);
        boolean isLiked = postLikeRepository.findByPostAndUser_UserId(post, currentUserId).isPresent();
        boolean isEchoed = postEchoRepository.findByPostAndUser_UserId(post, currentUserId).isPresent();

        EngagementDTO engagement = new EngagementDTO(likesCount, commentsCount, echoesCount, isLiked, isEchoed);

        return new TextPostDTO(
                post.getPostId(),
                post.getCategory(),
                post.getContent(),
                post.getCreatedAt(),
                engagement
        );
    }
}
