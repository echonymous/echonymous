package com.echonymous.service;

import com.echonymous.dto.CommentDTO;
import com.echonymous.dto.FeedResponseDTO;
import com.echonymous.entity.CommentLike;
import com.echonymous.entity.Post;
import com.echonymous.entity.PostComment;
import com.echonymous.entity.User;
import com.echonymous.repository.CommentLikeRepository;
import com.echonymous.repository.PostCommentRepository;
import com.echonymous.repository.PostRepository;
import com.echonymous.repository.UserRepository;
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
public class CommentService {
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    public CommentService(PostRepository postRepository, PostCommentRepository postCommentRepository, CommentLikeRepository commentLikeRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentDTO createComment(Long postId, Long userId, String comment, Long parentCommentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        PostComment postComment = new PostComment();
        postComment.setPost(post);
        postComment.setUser(user);
        postComment.setComment(comment);
        postComment.setCreatedAt(LocalDateTime.now());
        postComment.setUpdatedAt(LocalDateTime.now());

        if (parentCommentId != null) {
            PostComment parentComment = postCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found."));
            postComment.setParentComment(parentComment);
        }

        PostComment saved = postCommentRepository.save(postComment);

        return mapToCommentDTO(saved, userId);
    }

    @Transactional
    public CommentDTO updateComment(Long commentId, Long userId, String newComment) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found."));

        // Only the comment author can update
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("User not authorized to update this comment.");
        }

        comment.setComment(newComment);
        comment.setUpdatedAt(LocalDateTime.now());
        PostComment updatedComm = postCommentRepository.save(comment);

        return mapToCommentDTO(updatedComm, userId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found."));

        // Allow deletion if the user is the comment author or the post author
        if (!comment.getUser().getUserId().equals(userId)
                && !comment.getPost().getAuthorId().equals(userId)) {
            throw new RuntimeException("User not authorized to delete this comment.");
        }
        postCommentRepository.delete(comment);
    }

    public FeedResponseDTO<CommentDTO> getCommentsForPost(Long postId, String cursor, int limit,  Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found."));

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<PostComment> comments;
        LocalDateTime cursorDate = DateTimeUtils.parseCursor(cursor);
        if (cursorDate != null) {
            comments = postCommentRepository.findByPostAndParentCommentIsNullAndCreatedAtBeforeOrderByCreatedAtDesc(post, cursorDate, pageable);
        } else {
            comments = postCommentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtDesc(post, pageable);
        }

        // Determine if there's a next cursor
        boolean hasNext = comments.size() > limit;
        if (hasNext) {
            comments = comments.subList(0, limit);
        }
        // Next cursor is the createdAt of the last post in the list
        String nextCursor = null;
        if (!comments.isEmpty()) {
            LocalDateTime lastCreatedAt = comments.get(comments.size() - 1).getCreatedAt();
            nextCursor = lastCreatedAt.toString();
        }

        List<CommentDTO> commentDTOs = comments.stream()
                .map(comment -> mapToCommentDTO(comment, currentUserId))
                .collect(Collectors.toList());

        return new FeedResponseDTO<>(commentDTOs, nextCursor, hasNext);
    }

    public FeedResponseDTO<CommentDTO> getRepliesForComment(Long commentId, String cursor, int limit, Long currentUserId) {
        PostComment parent = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found."));

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<PostComment> replies;
        LocalDateTime cursorDate = DateTimeUtils.parseCursor(cursor);
        if (cursorDate != null) {
            replies = postCommentRepository.findByParentCommentAndCreatedAtBeforeOrderByCreatedAtDesc(parent, cursorDate, pageable);
        } else {
            replies = postCommentRepository.findByParentCommentOrderByCreatedAtDesc(parent, pageable);
        }

        boolean hasNext = replies.size() > limit;
        if (hasNext) {
            replies = replies.subList(0, limit);
        }

        String nextCursor = null;
        if (!replies.isEmpty()) {
            LocalDateTime lastCreatedAt = replies.get(replies.size() - 1).getCreatedAt();
            nextCursor = lastCreatedAt.toString();
        }

        List<CommentDTO> repliesDTOs = replies.stream()
                .map(reply -> mapToCommentDTO(reply, currentUserId))
                .collect(Collectors.toList());

        return new FeedResponseDTO<>(repliesDTOs, nextCursor, hasNext);
    }

    @Transactional
    public CommentDTO toggleCommentLike(Long commentId, Long userId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        Optional<CommentLike> existingCommentLike = commentLikeRepository.findByCommentAndUser(comment, user);
        if(existingCommentLike.isPresent()){
            commentLikeRepository.delete(existingCommentLike.get());
        } else {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUser(user);
            like.setLikedAt(LocalDateTime.now());
            commentLikeRepository.save(like);
        }
        return mapToCommentDTO(comment, userId);
    }

    /**
     * Maps PostComment (reply as well) entity to CommentDTO.
     */
    private CommentDTO mapToCommentDTO(PostComment comment, Long currentUserId) {
        int commentLikesCount = commentLikeRepository.countByComment(comment);
        boolean isCommentLiked = commentLikeRepository.findByCommentAndUser_UserId(comment, currentUserId).isPresent();
        int replyCount = (comment.getReplies() != null) ? comment.getReplies().size() : 0;
        Long parentCommentId = (comment.getParentComment() != null) ? comment.getParentComment().getPostCommentId() : null;

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCommentId(comment.getPostCommentId());
        commentDTO.setPostId(comment.getPost().getPostId());
        commentDTO.setUserId(comment.getUser().getUserId());
        commentDTO.setComment(comment.getComment());
        commentDTO.setCreatedAt(comment.getCreatedAt());
        commentDTO.setUpdatedAt(comment.getUpdatedAt());
        commentDTO.setCommentLikesCount(commentLikesCount);
        commentDTO.setCommentLiked(isCommentLiked);
        commentDTO.setParentCommentId(parentCommentId);
        commentDTO.setReplyCount(replyCount);
        // Determine if this comment was authored by the current user.
        commentDTO.setIsCurrentUserComment(comment.getUser().getUserId().equals(currentUserId));

        return commentDTO;
    }
}
