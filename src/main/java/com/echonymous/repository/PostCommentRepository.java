package com.echonymous.repository;

import com.echonymous.entity.Post;
import com.echonymous.entity.PostComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    // For top-level comments (no parent), with cursor filtering
    List<PostComment> findByPostAndParentCommentIsNullAndCreatedAtBeforeOrderByCreatedAtDesc(Post post, LocalDateTime cursor, Pageable pageable);

    // Without a cursor (first page)
    List<PostComment> findByPostAndParentCommentIsNullOrderByCreatedAtDesc(Post post, Pageable pageable);

    // For replies with cursor filtering
    List<PostComment> findByParentCommentAndCreatedAtBeforeOrderByCreatedAtDesc(PostComment parentComment, LocalDateTime cursor, Pageable pageable);

    // For replies without a cursor
    List<PostComment> findByParentCommentOrderByCreatedAtDesc(PostComment parentComment, Pageable pageable);
}
