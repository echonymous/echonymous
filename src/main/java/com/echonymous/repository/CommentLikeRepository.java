package com.echonymous.repository;

import com.echonymous.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndUser(PostComment comment, User user);
    Optional<CommentLike> findByCommentAndUser_UserId(PostComment comment, Long userId);

    int countByComment(PostComment comment);
}
