package com.echonymous.repository;

import com.echonymous.entity.TextPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TextPostRepository extends JpaRepository<TextPost, Long> {
    // Initial query: fetch the latest text posts
    List<TextPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // When a cursor is provided, fetch text posts whose createdAt is before the cursor
    List<TextPost> findByCreatedAtBeforeOrderByCreatedAtDesc(LocalDateTime cursor, Pageable pageable);

    // Fetch text posts filtered by category without cursor
    List<TextPost> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category, Pageable pageable);

    // Fetch text posts filtered by category with a cursor for pagination
    List<TextPost> findByCategoryIgnoreCaseAndCreatedAtBeforeOrderByCreatedAtDesc(String category, LocalDateTime cursor, Pageable pageable);

    // Get the posts for the given author sorted by createdAt descending
    List<TextPost> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    // When a cursor is provided (createdAt timestamp), get posts with createdAt before this value
    List<TextPost> findByAuthorIdAndCreatedAtBeforeOrderByCreatedAtDesc(Long authorId, LocalDateTime createdAt, Pageable pageable);
}
